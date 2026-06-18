package kaguya.user.domain.auth.service;

import kaguya.user.domain.auth.mapper.AuthMapper;
import kaguya.user.domain.auth.model.dto.request.AccountReq;
import kaguya.user.domain.auth.model.dto.request.LoginReq;
import kaguya.user.domain.auth.model.dto.request.RegisterReq;
import kaguya.user.domain.auth.model.dto.request.UserReq;
import kaguya.user.domain.auth.model.dto.response.CheckTokenRes;
import kaguya.user.domain.auth.model.dto.response.LoginRes;
import kaguya.user.domain.common.model.enums.Gender;
import kaguya.user.domain.common.model.enums.Role;
import kaguya.user.domain.common.repository.RedisRepository;
import kaguya.user.domain.user.model.entity.UserEntity;
import kaguya.user.domain.user.repository.UserRepository;
import kaguya.user.global.exception.BusinessException;
import kaguya.user.global.exception.ErrorCode;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    AuthService authService;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    JwtProvider jwtProvider;

    @Mock
    UserRepository userRepository;
    @Mock
    RedisRepository redisRepository;

    @Spy
    AuthMapper authMapper;
    @Captor
    ArgumentCaptor<UserEntity> userEntityCaptor;


    /**
     * 정상 테스트 (Happy Path Test)
     */
    @Test
    @DisplayName("회원가입 성공 - 데이터 매핑 및 암호화 검증")
    void 회원가입_테스트_성공() {
        // given
        AccountReq account = new AccountReq("testID", "testPassword12!@", "aaaa@bbbb.com", "user1");
        UserReq user = new UserReq("홍길동", LocalDate.now(), "010-1234-5678", Gender.MALE.toString());
        RegisterReq registerData = new RegisterReq(account, user);

        given(userRepository.existsByUsername(registerData.account().username())).willReturn(false);
        given(userRepository.existsByEmail(registerData.account().email())).willReturn(false);
        given(userRepository.existsByNickname(registerData.account().nickname())).willReturn(false);

        String expectedEncodedPassword = "encodedPassword12!@";
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
        LoginReq loginData = new LoginReq("testID", "testPassword12!@");

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

        willDoNothing().given(jwtProvider).validateRefreshToken(refreshToken);
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

        willDoNothing().given(jwtProvider).validateRefreshToken(refreshToken);
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

        willDoNothing().given(jwtProvider).validateAccessToken(accessToken);
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
    @Test
    @DisplayName("회원가입 - 이미 존재하는 아이디")
    void 회원가입_존재하는_아이디() {
        // given
        AccountReq account = new AccountReq("testID", "testPassword12!@", "bbbb@cccc.com", "user2");
        UserReq user = new UserReq("김철수", LocalDate.now(), "010-1111-2222", Gender.MALE.toString());
        RegisterReq registerData = new RegisterReq(account, user);

        // existsByUsername 검사 했을 때 true 라고 나올 경우 (아이디 중복일 경우)
        given(userRepository.existsByUsername(registerData.account().username())).willReturn(true);

        // when & then
        // 설정한 예외 처리가 올바르게 터지는지 확인
        assertThatThrownBy(() -> authService.register(registerData))
                .isInstanceOf(BusinessException.class)  // BusinessException 인지 체크
                .extracting("errorCode")  // errorCode 내용 가져와서
                .isEqualTo(ErrorCode.EXISTS_USERNAME);  // "EXISTS_USERNAME" 인지 확인
        // userRepository에 저장 되었는지 확인
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("회원가입 - 이미 존재하는 이메일")
    void 회원가입_존재하는_이메일() {
        // given
        AccountReq account = new AccountReq("test123", "testPassword12!@", "aaaa@bbbb.com", "user2");
        UserReq user = new UserReq("김철수", LocalDate.now(), "010-1111-2222", Gender.MALE.toString());
        RegisterReq registerData = new RegisterReq(account, user);

        // existsByEmail 검사 했을 때 true 라고 나올 경우 (이메일 중복일 경우)
        given(userRepository.existsByEmail(registerData.account().email())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.register(registerData))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EXISTS_EMAIL);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("회원가입 - 이미 존재하는 닉네임")
    void 회원가입_존재하는_닉네임() {
        // given
        AccountReq account = new AccountReq("test123", "testPassword12!@", "bbbb@cccc.com", "user1");
        UserReq user = new UserReq("김철수", LocalDate.now(), "010-1111-2222", Gender.MALE.toString());
        RegisterReq registerData = new RegisterReq(account, user);

        // existsByNickname 검사 했을 때 true 라고 나올 경우 (닉네임 중복일 경우)
        given(userRepository.existsByNickname(registerData.account().nickname())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.register(registerData))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EXISTS_NICKNAME);
        verify(userRepository, never()).save(any());
    }

    // todo. 실패 케이스
    // 로그인 - 아이디 찾지 못할 때
    // 로그인 - 아이디 비밀번호 다를때

    // 로그아웃 - accessToken 비어있을 때
    // 로그아웃 - refreshToken 유효하지 않을 때
    // 로그아웃 - refreshToken 만료되었을 때

    // 토큰갱신 - accessToken 유효하지 않을 때
    // 토큰갱신 - 아이디 찾을 수 없을 때

    // 토큰검사 - 블랙리스트에 등록된 accessToken을 사용했을 때



    /**
     * 헬퍼 메서드
     */
    private UserEntity createDefaultUser() {
        return UserEntity.builder()
                .username("testID")
                .password("encodedPassword12!@")
                .nickname("user1")
                .email("aaaa@bbbb.com")
                .name("홍길동")
                .birth(LocalDate.now())
                .phone("010-1234-5678")
                .gender(Gender.MALE)
                .build();
    }
}