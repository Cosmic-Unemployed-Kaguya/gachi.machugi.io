package kaguya.domain.user.service;

import kaguya.domain.user.model.dto.request.LoginReq;
import kaguya.domain.user.model.dto.request.RegisterReq;
import kaguya.domain.user.model.dto.response.LoginRes;
import kaguya.domain.user.model.entity.UserEntity;
import kaguya.domain.user.repository.RedisRepository;
import kaguya.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RedisRepository redisRepository;

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
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 사용된 이메일인지 확인
        if(userRepository.existsByEmail(registerData.account().email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 패스워드 설정
        String rawPassword = registerData.account().password();  // 암호화 전
        String encodedPassword = passwordEncoder.encode(rawPassword);  // 암호화

        UserEntity entity = UserEntity.builder()
                .username(registerData.account().username())
                .password(encodedPassword)
                .nickname(registerData.account().nickname())
                .email(registerData.account().email())
                .name(registerData.user().name())
                .birth(registerData.user().birth())
                .phone(registerData.user().phone())
                .build();

        userRepository.save(entity);
    }

    /**
     * 로그인 로직
     * @param loginData: Id, Password
     */
    @Transactional(readOnly = true)
    public LoginRes login(LoginReq loginData) {

        // 아이디 존재하는지 확인
        UserEntity entity = userRepository.findByUsername(loginData.username())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다."));

        // 아이디와 비밀번호 맞는지 검증
        if (!passwordEncoder.matches(loginData.password(), entity.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        // Access/Refresh 토큰
        String accessToken = jwtProvider.createAccessToken(entity.getUsername());
        String refreshToken = jwtProvider.createRefreshToken(entity.getUsername());

        // 갱신 토큰 Redis 저장 (14일)
        redisRepository.save("RT:" + entity.getUsername(), refreshToken, 14, TimeUnit.DAYS);

        // todo. mapper 도입
        return new LoginRes(accessToken, refreshToken, entity.getNickname());
    }

    /**
     * 로그아웃 로직
     * @param accessToken: 접근 토큰
     * @param refreshToken: 갱신 토큰
     */
    @Transactional
    public void logout(String accessToken, String refreshToken) {

        // 유효한 토큰인지 검증
        if(!jwtProvider.validationToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않는 갱신 토큰");
        }

        // Refresh Token으로 id 조회
        String username = jwtProvider.getUsername(refreshToken);

        // Redis
        redisRepository.delete("RT:" + username);  // Refresh 토큰 삭제
        redisRepository.save("BL:" + accessToken, "logout", 10, TimeUnit.MINUTES);  // Access 토큰 블랙리스트 등록
    }
}