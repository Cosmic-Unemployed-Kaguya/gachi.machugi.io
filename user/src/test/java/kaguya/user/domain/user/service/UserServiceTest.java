package kaguya.user.domain.user.service;

import kaguya.user.domain.common.model.enums.Gender;
import kaguya.user.domain.user.mapper.UserMapper;
import kaguya.user.domain.user.model.dto.request.UpdateNicknameReq;
import kaguya.user.domain.user.model.dto.request.UpdatePasswordReq;
import kaguya.user.domain.user.model.dto.response.MyPageRes;
import kaguya.user.domain.user.model.dto.response.ProfileReq;
import kaguya.user.domain.user.model.entity.UserEntity;
import kaguya.user.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    UserService userService;

    @Mock
    UserRepository userRepository;
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
        String username = "testID";
        UserEntity user = createUser();

        given(userRepository.findByUsername(username)).willReturn(Optional.of(user));

        // when
        MyPageRes result = userService.getMyPage(username);

        // then
        assertThat(result.username()).isEqualTo("testID");
        assertThat(result.email()).isEqualTo("aaaa@bbbb.com");
        assertThat(result.nickname()).isEqualTo("user1");
    }

    @Test
    @DisplayName("프로필 조회 성공")
    void 프로필_성공() {
        // given
        String username = "testID";
        UserEntity user = createUser();

        given(userRepository.findByUsername(username)).willReturn(Optional.of(user));

        // when
        ProfileReq result = userService.getProfile(username);

        // then
        assertThat(result.name()).isEqualTo("홍길동");
        assertThat(result.birth()).isEqualTo(user.getBirth().toString());
        assertThat(result.phone()).isEqualTo("010-1234-5678");
        assertThat(result.gender()).isEqualTo(Gender.MALE.toString());
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void 비밀번호_번경_성공() {
        // given
        String username = "testID";
        UserEntity user = createUser();

        UpdatePasswordReq request = new UpdatePasswordReq(
                "encodedPassword123",
                "changedPassword123"
        );

        given(userRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.currentPassword(), user.getPassword())).willReturn(true);
        given(passwordEncoder.matches(request.newPassword(), user.getPassword())).willReturn(false);
        given(passwordEncoder.encode(request.newPassword())).willReturn("encryptedNewPassword");

        // when
        userService.updatePassword(username, request);

        // then
        verify(passwordEncoder).matches(request.currentPassword(), "encodedPassword123");
        verify(passwordEncoder).encode(request.newPassword());
        assertThat(user.getPassword()).isEqualTo("encryptedNewPassword");
    }

    @Test
    @DisplayName("닉네임 변경 성공")
    void 닉네임_번경_성공() {
        // given
        String username = "testID";
        UserEntity user = createUser();

        UpdateNicknameReq request = new UpdateNicknameReq(
                "changedNickname"
        );

        given(userRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(userRepository.existsByNickname(request.nickname())).willReturn(false);

        // when
        userService.updateNickname(username, request);

        // then
        assertThat(user.getNickname()).isEqualTo("changedNickname");
    }

    @Test
    @DisplayName("회원탈퇴 성공")
    void 회원탈퇴_성공() {
        // given
        String username = "testID";
        UserEntity user = createUser();

        given(userRepository.findByUsername(username)).willReturn(Optional.of(user));
        willDoNothing().given(userRepository).delete(user);

        // when
        userService.withdraw(username);

        // then
        verify(userRepository, times(1)).delete(user);
    }


    /**
     * 비정상 테스트 (Negative Test)
     */
    // todo. 비정상 테스트

    /**
     * 헬퍼 메서드
     */
    private UserEntity createUser() {
        return UserEntity.builder()
                .username("testID")
                .password("encodedPassword123")
                .email("aaaa@bbbb.com")
                .nickname("user1")
                .name("홍길동")
                .birth(LocalDate.now())
                .phone("010-1234-5678")
                .gender(Gender.MALE)
                .build();
    }
}