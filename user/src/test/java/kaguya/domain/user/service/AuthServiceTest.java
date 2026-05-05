package kaguya.domain.user.service;

import kaguya.domain.user.model.dto.AccountDTO;
import kaguya.domain.user.model.dto.UserDTO;
import kaguya.domain.user.model.dto.request.LoginReq;
import kaguya.domain.user.model.dto.request.RegisterReq;
import kaguya.domain.user.model.dto.response.CheckTokenRes;
import kaguya.domain.user.model.dto.response.LoginRes;
import kaguya.domain.user.model.entity.UserEntity;
import kaguya.domain.user.model.enums.Gender;
import kaguya.domain.user.model.enums.Role;
import kaguya.domain.user.repository.RedisRepository;
import kaguya.domain.user.repository.UserRepository;
import kaguya.domain.user.util.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks AuthService authService;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtProvider jwtProvider;

    @Mock UserRepository userRepository;
    @Mock RedisRepository redisRepository;

    @Spy UserMapper userMapper;
    @Captor ArgumentCaptor<UserEntity> userEntityCaptor;


    /**
     * 정상 테스트 (Happy Path Test)
     */
    @Test
    @DisplayName("회원가입 성공 - 데이터 매핑 및 암호화 검증")
    void 회원가입_테스트_성공() {
        // given
        AccountDTO account = new AccountDTO("testID", "testPassword", "user1", "aaaa@bbbb.com");
        UserDTO user = new UserDTO("홍길동", LocalDate.now(), "010-1234-5678", Gender.MALE.toString());
        RegisterReq registerData = new RegisterReq(account, user);

        given(userRepository.existsByUsername(registerData.account().username())).willReturn(false);
        given(userRepository.existsByEmail(registerData.account().email())).willReturn(false);

        String expectedEncodedPassword = "encodedPassword123!";
        given(passwordEncoder.encode(registerData.account().password())).willReturn(expectedEncodedPassword);

        // when
        authService.register(registerData);

        // then
        verify(userRepository).save(userEntityCaptor.capture());
        UserEntity savedEntity = userEntityCaptor.getValue();

        assertThat(savedEntity.getUsername()).isEqualTo("testID");
        assertThat(savedEntity.getEmail()).isEqualTo("aaaa@bbbb.com");
        assertThat(savedEntity.getPassword()).isEqualTo(expectedEncodedPassword);
    }

    @Test
    @DisplayName("로그인 성공 (쿠키 생성)")
    void 로그인_테스트_성공() {
        //given
        LoginReq loginData = new LoginReq("testID", "testPassword");

        UserEntity mockEntity = createDefaultUser();

        given(userRepository.findByUsername(loginData.username())).willReturn(Optional.of(mockEntity));
        given(passwordEncoder.matches(loginData.password(), mockEntity.getPassword())).willReturn(true);

        String accessToken = "accessToken-aaabbbccc";
        String refreshToken = "refreshToken-dddeeefff";
        given(jwtProvider.createAccessToken(mockEntity.getUsername(), mockEntity.getRole().toString())).willReturn(accessToken);
        given(jwtProvider.createRefreshToken(mockEntity.getUsername())).willReturn(refreshToken);

        // when
        LoginRes result = authService.login(loginData);

        // then
        verify(redisRepository).save(
                eq("RT:" + mockEntity.getUsername()),
                eq(refreshToken),
                eq(14L),
                eq(TimeUnit.DAYS)
        );

        assertThat(result.accessToken()).isEqualTo(accessToken);
        assertThat(result.refreshToken()).isEqualTo(refreshToken);
        assertThat(result.nickname()).isEqualTo(mockEntity.getNickname());
    }

    @Test
    @DisplayName("로그아웃 성공 (쿠키 확인)")
    void 로그아웃_성공() {
        // given
        String accessToken = "accessToken-aaabbbccc";
        String refreshToken = "refreshToken-dddeeefff";
        String username = "testID";

        given(jwtProvider.validationToken(refreshToken)).willReturn(true);
        given(jwtProvider.getUsername(refreshToken)).willReturn(username);

        // when
        authService.logout(accessToken, refreshToken);

        // then
        verify(redisRepository).save(
                eq("BL:" + accessToken),
                eq("logout"),
                eq(10L),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    @DisplayName("토큰갱신 성공")
    void 토큰갱신_성공() {
        // given
        String refreshToken = "refreshToken-dddeeefff";
        String username = "testID";
        String accessToken = "accessToken-AAABBBCCC";

        UserEntity mockEntity = createDefaultUser();

        given(jwtProvider.validationToken(refreshToken)).willReturn(true);
        given(jwtProvider.getUsername(refreshToken)).willReturn(username);
        given(redisRepository.get("RT:" + username)).willReturn(refreshToken);
        given(userRepository.findByUsername(username)).willReturn((Optional.of(mockEntity)));
        given(jwtProvider.createAccessToken(mockEntity.getUsername(), mockEntity.getRole().toString())).willReturn(accessToken);

        // when
        String result = authService.renewToken(refreshToken);

        // then
        assertThat(result).isEqualTo(accessToken);
    }


    @Test
    @DisplayName("토큰확인 성공")
    void 토큰확인_성공() {
        // given
        String accessToken = "accessToken-aaabbbccc";
        String username = "testID";
        String role = Role.USER.toString();

        given(jwtProvider.validationToken(accessToken)).willReturn(true);
        given(redisRepository.exist("BL:" + accessToken)).willReturn(false);
        given(jwtProvider.getUsername(accessToken)).willReturn(username);
        given(jwtProvider.getRole(accessToken)).willReturn(role);

        // when
        CheckTokenRes result = authService.checkToken(accessToken);

        // then
        verify(redisRepository).exist("BL:" + accessToken);
        assertThat(result.username()).isEqualTo(username);
        assertThat(result.role()).isEqualTo(role);
    }

    /**
     * 비정상 테스트 (Negative Test)
     */
    // todo. 비정상 테스트


    /**
     * 헬퍼 메서드
     */
    private UserEntity createDefaultUser() {
        return UserEntity.builder()
                .username("testID")
                .password("encodedPassword123")
                .nickname("user1")
                .email("aaaa@bbbb.com")
                .name("홍길동")
                .birth(LocalDate.now())
                .phone("010-1234-5678")
                .gender(Gender.MALE)
                .build();
    }
}