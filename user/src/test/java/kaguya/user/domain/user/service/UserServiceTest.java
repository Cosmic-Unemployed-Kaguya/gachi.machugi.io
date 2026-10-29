package kaguya.user.domain.user.service;

import kaguya.user.domain.common.repository.RedisRepository;
import kaguya.user.domain.user.mapper.UserMapper;
import kaguya.user.domain.user.model.dto.request.ResetPasswordReq;
import kaguya.user.domain.user.model.dto.request.UpdateNicknameReq;
import kaguya.user.domain.user.model.dto.request.UpdatePasswordReq;
import kaguya.user.domain.user.model.dto.response.MyPageRes;
import kaguya.user.domain.user.model.dto.response.ProfileRes;
import kaguya.user.domain.user.model.entity.UserEntity;
import kaguya.user.domain.user.model.entity.UserProfileEntity;
import kaguya.user.domain.user.model.enums.Gender;
import kaguya.user.domain.user.model.enums.Status;
import kaguya.user.domain.user.repository.UserProfileRepository;
import kaguya.user.domain.user.repository.UserRepository;
import kaguya.user.domain.verification.model.enums.VerificationType;
import kaguya.user.global.exception.BusinessException;
import kaguya.user.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    UserService userService;

    @Mock
    UserRepository userRepository;
    @Mock
    UserProfileRepository userProfileRepository;
    @Mock
    RedisRepository redisRepository;
    @Mock
    PasswordEncoder passwordEncoder;

    @Spy
    UserMapper userMapper;

    /**
     * 정상 테스트 (Happy Path)
     */
    @Test
    @DisplayName("마이페이지 조회 성공")
    void 마이페이지_성공 () {
        // given
        UserEntity user = createUser();
        Long userIdx = user.getIdx();

        given(userRepository.findById(userIdx)).willReturn(Optional.of(user));

        // when
        MyPageRes result = userService.getMyPage(userIdx);

        // then
        assertThat(result.email()).isEqualTo("aaaa@bbbb.com");
        assertThat(result.nickname()).isEqualTo("user1");
    }

    @Test
    @DisplayName("프로필 조회 성공")
    void 프로필_성공() {
        // given
        UserEntity user = createUser();
        ReflectionTestUtils.setField(user, "idx", 1L);
        Long userIdx = user.getIdx();
        UserProfileEntity profile = createUserProfile(userIdx);

        given(userProfileRepository.findById(userIdx)).willReturn(Optional.of(profile));

        // when
        ProfileRes result = userService.getProfile(userIdx);

        // then
        assertThat(result.name()).isEqualTo("홍길동");
        assertThat(result.birth()).isEqualTo(profile.getBirth());
        assertThat(result.phone()).isEqualTo("010-0000-0000");
        assertThat(result.gender()).isEqualTo(Gender.MALE.toString());
    }

    @Test
    @DisplayName("닉네임 변경 성공")
    void 닉네임_번경_성공() {
        // given
        UserEntity user = createUser();
        Long userIdx = user.getIdx();

        UpdateNicknameReq request = new UpdateNicknameReq("changedNickname");

        given(userRepository.findById(userIdx)).willReturn(Optional.of(user));
        given(userRepository.existsByNickname(request.nickname())).willReturn(false);

        // when
        userService.updateNickname(userIdx, request.nickname());

        // then
        assertThat(user.getNickname()).isEqualTo("changedNickname");
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void 비밀번호_번경_성공() {
        // given
        UserEntity user = createUser();
        Long userIdx = user.getIdx();
        String originalPassword = user.getPassword();

        UpdatePasswordReq request = new UpdatePasswordReq(
                "encodedPassword123",
                "changedPassword123"
        );

        given(userRepository.findById(userIdx)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.currentPassword(), user.getPassword())).willReturn(true);
        given(passwordEncoder.matches(request.newPassword(), originalPassword)).willReturn(false);
        given(passwordEncoder.encode(request.newPassword())).willReturn("encryptedNewPassword");

        // when
        userService.updatePassword(userIdx, request);

        // then
        verify(passwordEncoder).matches(request.currentPassword(), "encodedPassword123");
        verify(passwordEncoder).encode(request.newPassword());
        verify(redisRepository).delete("RT:" + user.getIdx());
        assertThat(user.getPassword()).isEqualTo("encryptedNewPassword");
    }

    @Test
    @DisplayName("회원탈퇴 성공")
    void 회원탈퇴_성공() {
        // given
        UserEntity user = createUser();
        ReflectionTestUtils.setField(user, "idx", 1L);

        Long userIdx = user.getIdx();

        given(userRepository.findById(userIdx)).willReturn(Optional.of(user));

        // when
        userService.withdraw(userIdx);

        // then
        assertThat(user.getStatus()).isEqualTo(Status.WITHDRAWAL);
        assertThat(user.getWithdrawalDate()).isNotNull();
    }

    @Test
    @DisplayName("비밀번호 초기화")
    void 비밀번호_초기화() {
        // given
        UserEntity user = createUser();
        ReflectionTestUtils.setField(user, "idx", 1L);

        String oneTimeAuthCode = UUID.randomUUID().toString();
        ResetPasswordReq request = new ResetPasswordReq(oneTimeAuthCode, "newPassword123!@#");

        String oneTimeKey = "verification:oneTimeAuthCode:" + VerificationType.RESET_PASSWORD.name() + ":" + oneTimeAuthCode;
        given(redisRepository.get(oneTimeKey)).willReturn(user.getEmail());

        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.newPassword(), user.getPassword())).willReturn(false);

        String expectedEncodedNewPassword = "encodedNewPassword12!@";
        given(passwordEncoder.encode(request.newPassword())).willReturn(expectedEncodedNewPassword);

        // when
        userService.resetPassword(request);

        // then
        assertThat(user.getPassword()).isEqualTo(expectedEncodedNewPassword);
        verify(redisRepository).delete(oneTimeKey);
        verify(redisRepository).delete("RT:" + user.getIdx());
    }


    /**
     * 비정상 테스트 (Negative Test)
     */
    @Test
    @DisplayName("마이페이지 - 존재하지 않는 사용자")
    void 마이페이지_존재하지_않는_사용자() {
        // given
        UserEntity user = createUser();
        Long userIdx = user.getIdx();

        given(userRepository.findById(userIdx)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getMyPage(userIdx))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("닉네임 변경 - 기존 닉네임과 동일")
    void 닉네임_변경_기존_닉네임과_동일() {
        // given
        UserEntity user = createUser();
        Long userIdx = user.getIdx();
        UpdateNicknameReq req = new UpdateNicknameReq("user1");

        given(userRepository.findById(userIdx)).willReturn(Optional.of(user));
        // (service 로직) 기존 닉네임과 바꿀 닉네임 비교

        // when & then
        assertThatThrownBy(() -> userService.updateNickname(userIdx, req.nickname()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SAME_AS_OLD_NICKNAME);
    }

    @Test
    @DisplayName("닉네임 변경 - 이미 존재하는 닉네임")
    void 닉네임_변경_이미_존재하는_닉네임() {
        // given
        UserEntity user = createUser();
        Long userIdx = user.getIdx();
        UpdateNicknameReq req = new UpdateNicknameReq("user2");

        given(userRepository.findById(userIdx)).willReturn(Optional.of(user));
        given(userRepository.existsByNickname(req.nickname())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.updateNickname(userIdx, req.nickname()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EXISTS_NICKNAME);
    }

    @Test
    @DisplayName("비밀번호 변경 - 현재 비밀번호 불일치")
    void 비밀번호_변경_현재_비밀번호_불일치() {
        // given
        UserEntity user = createUser();
        Long userIdx = user.getIdx();
        UpdatePasswordReq req = new UpdatePasswordReq("wrongCurrentPassword", "newPassword12!");

        given(userRepository.findById(userIdx)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(req.currentPassword(), user.getPassword())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.updatePassword(userIdx, req))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_CURRENT_PASSWORD);
    }

    @Test
    @DisplayName("비밀번호 변경 - 기존 비밀번호와 동일")
    void 비밀번호_변경_기존_비밀번호와_동일() {
        // given
        UserEntity user = createUser();
        Long userIdx = user.getIdx();
        UpdatePasswordReq req = new UpdatePasswordReq("correctCurrentPassword", "sameOldPassword");

        given(userRepository.findById(userIdx)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(req.currentPassword(), user.getPassword())).willReturn(true);
        given(passwordEncoder.matches(req.newPassword(), user.getPassword())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.updatePassword(userIdx, req))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SAME_AS_OLD_PASSWORD);
    }

    @Test
    @DisplayName("회원탈퇴 - 회원이 없음")
    void 탈퇴_회원을_찾을_수_없음() {

        // given
        Long userIdx = 123L;  // 없는 유저 idx
        given(userRepository.findById(userIdx)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.withdraw(userIdx))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("비밀번호 초기화 실패 - 유효하지 않은 인증코드")
    void 비밀번호_초기화_인증코드_유효하지_않음() {
        // given
        String oneTimeAuthCode = UUID.randomUUID().toString();
        ResetPasswordReq request = new ResetPasswordReq(oneTimeAuthCode, "newPassword123!@#");

        String oneTimeKey = "verification:oneTimeAuthCode:" + VerificationType.RESET_PASSWORD.name() + ":" + oneTimeAuthCode;
        given(redisRepository.get(oneTimeKey)).willReturn(null);  // 인증코드 조회되지 않음

        // when & then
        assertThatThrownBy(() -> userService.resetPassword(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_AUTH_CODE);

        // then
        verify(redisRepository, never()).delete(anyString());  // 키 삭제 안됨
        verifyNoInteractions(userRepository, passwordEncoder);  // userRepository, passwordEncode 동작하지 않음
    }

    @Test
    @DisplayName("비밀번호 초기화 - 유저를 찾을 수 없음")
    void 비밀번호_초기화_존재하지_않는_유저() {
        // given
        UserEntity user = createUser();

        String oneTimeAuthCode = UUID.randomUUID().toString();
        ResetPasswordReq request = new ResetPasswordReq(oneTimeAuthCode, "newPassword123!@#");

        String oneTimeKey = "verification:oneTimeAuthCode:" + VerificationType.RESET_PASSWORD.name() + ":" + oneTimeAuthCode;
        given(redisRepository.get(oneTimeKey)).willReturn(user.getEmail());

        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.resetPassword(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);

        // then
        verify(redisRepository, never()).delete(anyString());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("비밀번호 초기화 - 이전 비밀번호와 동일")
    void 비밀번호_초기화_이전_비밀번호와_동일() {
        // given
        UserEntity user = createUser();

        String oneTimeAuthCode = UUID.randomUUID().toString();
        ResetPasswordReq request = new ResetPasswordReq(oneTimeAuthCode, "newPassword123!@#");

        String oneTimeKey = "verification:oneTimeAuthCode:" + VerificationType.RESET_PASSWORD.name() + ":" + oneTimeAuthCode;
        given(redisRepository.get(oneTimeKey)).willReturn(user.getEmail());

        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.newPassword(), user.getPassword())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.resetPassword(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SAME_AS_OLD_PASSWORD);

        assertThat(user.getPassword()).isEqualTo("encodedPassword123");
        verify(redisRepository, never()).delete(anyString());
        verify(passwordEncoder, never()).encode(anyString());
    }


    /**
     * 헬퍼 메서드
     */
    private UserEntity createUser() {
        return UserEntity.builder()
                .email("aaaa@bbbb.com")
                .password("encodedPassword123")
                .nickname("user1")
                .build();
    }

    private UserProfileEntity createUserProfile(Long userIdx) {
        return UserProfileEntity.builder()
                .userIdx(userIdx)
                .name("홍길동")
                .birth(LocalDate.now())
                .phone("010-0000-0000")
                .gender(Gender.MALE)
                .build();
    }
}