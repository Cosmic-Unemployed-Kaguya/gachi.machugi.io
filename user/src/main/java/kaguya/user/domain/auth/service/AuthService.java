package kaguya.user.domain.auth.service;

import kaguya.user.domain.auth.mapper.AuthMapper;
import kaguya.user.domain.auth.model.dto.request.GuestReq;
import kaguya.user.domain.auth.model.dto.request.LoginReq;
import kaguya.user.domain.auth.model.dto.request.RegisterReq;
import kaguya.user.domain.auth.model.dto.response.CheckTokenRes;
import kaguya.user.domain.auth.model.dto.response.GuestRes;
import kaguya.user.domain.auth.model.dto.response.LoginRes;
import kaguya.user.domain.common.repository.RedisRepository;
import kaguya.user.domain.user.model.entity.UserEntity;
import kaguya.user.domain.user.repository.UserRepository;
import kaguya.user.global.exception.BusinessException;
import kaguya.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RedisRepository redisRepository;

    private final AuthMapper authMapper;

    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    /**
     * todo.
     * - 회원가입 시 사용자 개인정보 (이름, 생년월일, 핸드폰 번호) 양방향 암호화
     */

    /**
     * 회원가입 로직
     * @param registerData: 회원가입 정보
     * - AccountDTO: username, password, nickname, email
     * - UserDTO: name, birth, phone, gender
     */
    @Transactional
    public void register(RegisterReq registerData) {

        // 사용된 아이디인지 확인
        if(userRepository.existsByUsername(registerData.account().username())) {
            throw new BusinessException(ErrorCode.EXISTS_USERNAME);
        }

        // 사용된 이메일인지 확인
        if(userRepository.existsByEmail(registerData.account().email())) {
            throw new BusinessException(ErrorCode.EXISTS_EMAIL);
        }

        // 사용된 닉네임인지 확인
        if(userRepository.existsByNickname(registerData.account().nickname())) {
            throw new BusinessException(ErrorCode.EXISTS_NICKNAME);
        }

        // 패스워드 설정
        String rawPassword = registerData.account().password();  // 암호화 전
        String encodedPassword = passwordEncoder.encode(rawPassword);  // 암호화

        UserEntity entity = authMapper.userDtoToEntity(registerData, encodedPassword);
        userRepository.save(entity);
    }

    /**
     * 로그인 로직
     * @param loginData: Id, Password
     */
    @Transactional
    public LoginRes login(LoginReq loginData) {

        // 아이디 존재하는지 확인
        UserEntity entity = userRepository.findByUsername(loginData.username())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        // 아이디와 비밀번호 맞는지 검증
        if (!passwordEncoder.matches(loginData.password(), entity.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // Access/Refresh 토큰
        String accessToken = jwtProvider.createAccessToken(entity.getUsername(), entity.getRole().toString());
        String refreshToken = jwtProvider.createRefreshToken(entity.getUsername());

        // 갱신 토큰 Redis 저장 (14일)
        redisRepository.save("RT:" + entity.getUsername(), refreshToken, 14, TimeUnit.DAYS);

        return authMapper.entityToLoginRes(accessToken, refreshToken, entity);
    }

    /**
     * 로그아웃 로직
     * @param accessToken: 접근 토큰
     * @param refreshToken: 갱신 토큰
     */
    public void logout(String accessToken, String refreshToken) {

        // accessToken이 비어있으면 로그인 상태가 아님
        if (accessToken == null) {
            throw new BusinessException(ErrorCode.MISSING_TOKEN);
        }

        // 로그아웃 시 갱신 토큰 삭제
        if (refreshToken != null) {
            try {
                // 유효한 토큰인지 검증
                jwtProvider.validateRefreshToken(refreshToken);

                // 검증을 통과했다면 갱신 토큰 삭제
                String username = jwtProvider.getUsername(refreshToken);
                redisRepository.delete("RT:" + username);

            } catch (BusinessException e) {
                // 토큰이 이미 만료되었거나 손상된 경우
                // 로그아웃을 방해하지 않기 위해 예외를 던지지 않음
            }
        }

        // Access 토큰 블랙리스트 등록
        redisRepository.save("BL:" + accessToken, "logout", 10, TimeUnit.MINUTES);  // Access 토큰 블랙리스트 등록
    }

    /**
     * 토큰 갱신
     * @param refreshToken: 갱신 토큰
     */
    @Transactional(readOnly = true)
    public String renewToken(String refreshToken) {

        // refreshToken이 비어있으면 에러 반환
        if (refreshToken == null) {
            throw new BusinessException(ErrorCode.MISSING_TOKEN);
        }

        // 유효한 토큰인지 검증
        jwtProvider.validateRefreshToken(refreshToken);

        // 아이디 조회 (jwt)
        String username = jwtProvider.getUsername(refreshToken);

        // redis에 갱신 토큰이 있는지 확인
        String savedRefreshToken = redisRepository.get("RT:" + username);
        if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        UserEntity entity = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return jwtProvider.createAccessToken(username, entity.getRole().toString());
    }

    /**
     * 토큰 확인
     * @param accessToken: 접근 토큰
     */
    @Transactional(readOnly = true)
    public CheckTokenRes checkToken(String accessToken) {

        // accessToken이 비어있으면 에러 반환
        if (accessToken == null) {
            throw new BusinessException(ErrorCode.MISSING_TOKEN);
        }

        // 유요한 토큰인지 검증
        jwtProvider.validateAccessToken(accessToken);

        // 블랙 리스트 검사
        if(redisRepository.exist("BL:" + accessToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // 아이디 및 권한 조회 (jwt)
        String username = jwtProvider.getUsername(accessToken);
        String role = jwtProvider.getRole(accessToken);

        return new CheckTokenRes(username, role);
    }

    public GuestRes guest(GuestReq guestData) {

        String guestId = UUID.randomUUID().toString();
        String key = "GUEST:" + guestId;
        String guestNickname = "GUEST-" + guestData.nickname();

        // 게스트 유저 정보는 5시간 관리
        redisRepository.save(key, guestNickname, 5, TimeUnit.HOURS);

        return new GuestRes(guestId, guestNickname);
    }
}