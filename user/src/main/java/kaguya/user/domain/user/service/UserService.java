package kaguya.user.domain.user.service;

import kaguya.user.domain.common.repository.RedisRepository;
import kaguya.user.domain.user.mapper.UserMapper;
import kaguya.user.domain.user.model.dto.request.ResetPasswordReq;
import kaguya.user.domain.user.model.dto.request.UpdateNicknameReq;
import kaguya.user.domain.user.model.dto.request.UpdatePasswordReq;
import kaguya.user.domain.user.model.dto.response.MyPageRes;
import kaguya.user.domain.user.model.dto.response.ProfileReq;
import kaguya.user.domain.user.model.entity.UserEntity;
import kaguya.user.domain.user.repository.UserRepository;
import kaguya.user.global.exception.BusinessException;
import kaguya.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RedisRepository redisRepository;

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // 계정 정보 조회 (마이페이지)
    @Transactional(readOnly = true)
    public MyPageRes getMyPage(String username) {

        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return userMapper.entityToMyPageReq(userEntity);
    }

    // 사용자 정보 조회
    @Transactional(readOnly = true)
    public ProfileReq getProfile(String username) {

        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return userMapper.entityToUserReq(userEntity);
    }

    // 닉네임 변경
    @Transactional
    public void updateNickname(String username, UpdateNicknameReq updateNicknameData) {

        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 기존 닉네임과 완전히 동일한 경우 400 에러 처리
        if (userEntity.getNickname().equals(updateNicknameData.nickname())) {
            throw new BusinessException(ErrorCode.SAME_AS_OLD_NICKNAME);
        }

        // 닉네임 중복인지 확인
        if (userRepository.existsByNickname(updateNicknameData.nickname())) {
            throw new BusinessException(ErrorCode.EXISTS_NICKNAME);
        }

        userEntity.changeNickname(updateNicknameData.nickname());
    }

    // 비밀번호 변경
    @Transactional
    public void updatePassword(String username, UpdatePasswordReq updatePasswordData) {

        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(updatePasswordData.currentPassword(), userEntity.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CURRENT_PASSWORD);
        }

        if (passwordEncoder.matches(updatePasswordData.newPassword(), userEntity.getPassword())) {
            throw new BusinessException(ErrorCode.SAME_AS_OLD_PASSWORD);
        }

        String encodedPassword = passwordEncoder.encode(updatePasswordData.newPassword());
        userEntity.changePassword(encodedPassword);
    }

    // 회원탈퇴
    @Transactional
    public void withdraw(String username) {

        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        userRepository.delete(userEntity);
    }

    @Transactional(readOnly = true)
    public String getNicknameByUsername(String username) {

        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return userEntity.getNickname();
    }

    @Transactional(readOnly = true)
    public String findUsername(String oneTimeAuthCode) {

        // 일회용 인증번호 조회 및 저장된 이메일 가져오기
        String verifiedEmail = redisRepository.get(oneTimeAuthCode);
        if (verifiedEmail == null) {
            throw new BusinessException(ErrorCode.INVALID_AUTH_CODE);
        }
        redisRepository.delete(oneTimeAuthCode);

        UserEntity userEntity = userRepository.findByEmail(verifiedEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // username 마스킹 처리 후 반환
        return masking(userEntity.getUsername());
    }

    @Transactional
    public void resetPassword(ResetPasswordReq request) {

        String oneTimeAuthCode = request.oneTimeAuthCode();
        String newPassword = request.newPassword();

        String verifiedEmail = redisRepository.get(oneTimeAuthCode);
        if (verifiedEmail == null) {
            throw new BusinessException(ErrorCode.INVALID_AUTH_CODE);
        }
        redisRepository.delete(oneTimeAuthCode);

        UserEntity userEntity = userRepository.findByEmail(verifiedEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String encodedPassword = passwordEncoder.encode(newPassword);
        userEntity.changePassword(encodedPassword);
    }

    private String masking(String username) {

        if (username == null || username.isBlank()) {
            log.error("잘못 저장된 아이디(username)");
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // 만약 6자리 미만 username이 있다면 (그럴리 없겠지만)
        if (username.length() < 6) {
            // 4글자 (마스킹처리 X)
            if (username.length() <= 4) {
                return username;
            }
            // 5글자 (ex. abcde -> ab***)
            return username.substring(0, 2) + "***";
        }

        // 기본적으로 아이디 길이는 6~12자리
        // 앞 2자리는 노출하고, 뒤 3자리는 마스킹 처리 (ex. abcdefg -> ab***fg)
        return username.substring(0, 2) + "***" + username.substring(5);
    }
}
