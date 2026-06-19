package kaguya.user.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import kaguya.user.domain.auth.model.dto.request.AccountReq;
import kaguya.user.domain.auth.model.dto.request.LoginReq;
import kaguya.user.domain.auth.model.dto.request.RegisterReq;
import kaguya.user.domain.auth.model.dto.request.UserReq;
import kaguya.user.domain.auth.model.dto.response.LoginRes;
import kaguya.user.domain.auth.service.AuthService;
import kaguya.user.domain.common.model.enums.Gender;
import kaguya.user.global.exception.BusinessException;
import kaguya.user.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)  // spring security 필터 무시
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AuthService authService;

    /**
     * 정상 테스트 (Happy Path)
     */
    @Test
    @DisplayName("회원가입 성공")
    void 회원가입_테스트_성공() throws Exception {
        AccountReq account = new AccountReq("testID", "testPassword12!@", "aaaa@bbbb.com", "user1");
        UserReq user = new UserReq("홍길동", LocalDate.now(), "010-1234-5678", Gender.MALE.toString());
        RegisterReq register = new RegisterReq(account, user);

        // 회원가입 서비스 return이 null이어서 given 의미 없음

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("201"))
                .andExpect(jsonPath("$.message").value("회원가입 성공"));
    }


    @Test
    @DisplayName("로그인 성공 (쿠키 생성)")
    void 로그인_테스트_성공() throws Exception {
        LoginReq request = new LoginReq("testID", "testPassword");
        LoginRes response = new LoginRes("accessToken-aaabbbccc", "refreshToken-dddeeefff", "user1");

        given(authService.login(any(LoginReq.class)))
                .willReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // 응답 검증
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("로그인 성공"))
                .andExpect(jsonPath("$.data").value("user1"))

                // Access 쿠키 검증
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().value("accessToken", "accessToken-aaabbbccc"))
                .andExpect(cookie().httpOnly("accessToken", true))
                .andExpect(cookie().maxAge("accessToken", 10 * 60))

                // Refresh 쿠기 검증
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().value("refreshToken", "refreshToken-dddeeefff"))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().maxAge("refreshToken", 14 * 24 * 60 * 60));
    }

    @Test
    @DisplayName("로그아웃 성공 (쿠키 확인)")
    void 로그아웃_성공() throws Exception {
        Cookie access = new Cookie("accessToken", "accessToken-aaabbbccc");
        Cookie refresh = new Cookie("refreshToken", "refreshToken-dddeeefff");

        // 로그아웃 서비스 return이 null이어서 given 의미 없음

        mockMvc.perform(post("/auth/logout")
                        .cookie(access, refresh))

                // 응답 검증
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("로그아웃 성공"))

                // 쿠키 삭제 검증
                .andExpect(cookie().value("accessToken", ""))
                .andExpect(cookie().maxAge("accessToken", 0))
                .andExpect(cookie().value("refreshToken", ""))
                .andExpect(cookie().maxAge("refreshToken", 0));
    }

    @Test
    @DisplayName("토큰갱신 성공")
    void 토큰갱신_성공() throws Exception {
        Cookie refresh = new Cookie("refreshToken", "refreshToken-dddeeefff");
        String accessToken = "accessToken-AAABBBCCC";

        given(authService.renewToken(any(String.class)))
                .willReturn(accessToken);

        mockMvc.perform(post("/auth/reissue")
                        .cookie(refresh))

                // 응답 검증
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("토큰 갱신"))

                // Access 쿠키 검증
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().value("accessToken", "accessToken-AAABBBCCC"));
    }


    /**
     * 비정상 테스트 (Negative Test)
     */
    @Test
    @DisplayName("존재하지 않는 API 요청")
    void 존재하지_않는_API_요청() throws Exception {
        AccountReq account = new AccountReq("testID", "testPassword12!@", "aaaa@bbbb.com", "user1");
        UserReq user = new UserReq("홍길동", LocalDate.now(), "010-1234-5678", Gender.MALE.toString());
        RegisterReq register = new RegisterReq(account, user);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))

                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("404_PAGE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("페이지를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("회원가입 - 이메일 형식 다름")
    void 회원가입_이메일_형식_다름() throws Exception {
        // 이메일 형식이 아님
        AccountReq account = new AccountReq("testID", "testPassword12!@", "aaaa123", "user1");
        UserReq user = new UserReq("홍길동", LocalDate.now(), "010-1234-5678", Gender.MALE.toString());
        RegisterReq register = new RegisterReq(account, user);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400_INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message").value("이메일 형식이 올바르지 않습니다."));
    }

    @Test
    @DisplayName("회원가입 - 비밀번호 정규식 위반")
    void 회원가입_비밀번호_정규식_위반() throws Exception{
        // 비밀번호 특수문자(!@#) 안들어가 있음
        AccountReq account = new AccountReq("testID", "testPassword", "aaaa@bbbb.com", "user1");
        UserReq user = new UserReq("홍길동", LocalDate.now(), "010-1234-5678", Gender.MALE.toString());
        RegisterReq register = new RegisterReq(account, user);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400_INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message").value("비밀번호는 8~20자리이며, 영문, 숫자, 특수문자를 포함해야 합니다."));
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치 (service에서 커스텀 예외가 잘 throws 되는지 확인)")
    void 로그인_비밀번호_불일치() throws Exception {
        // 아이디와 비밀번호가 비어있는 요청
        LoginReq request = new LoginReq("testID", "testPassword12!@");

        // 커스텀 예외 처리가 정상적으로 동작 되는지
        given(authService.login(any(LoginReq.class)))
                .willThrow(new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("401_INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("로그아웃 - Access Token 누락")
    void 로그아웃_AccessToken_누락() throws Exception {
        // Access Token 없음
        String access = null;
        Cookie refresh = new Cookie("refreshToken", "refreshToken-dddeeefff");

        willThrow(new BusinessException(ErrorCode.MISSING_TOKEN))
                .given(authService).logout(access, refresh.getValue());

        mockMvc.perform(post("/auth/logout")
                .cookie(refresh))

                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("401_MISSING_TOKEN"))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    @Test
    @DisplayName("토큰갱신 - Refresh Token 누락")
    void 토큰갱신_RefreshToken_누락() throws Exception {
        // Refresh Token 없음
        String refresh = null;

        willThrow(new BusinessException(ErrorCode.MISSING_TOKEN))
                .given(authService).renewToken(refresh);

        mockMvc.perform(post("/auth/reissue"))

                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("401_MISSING_TOKEN"))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }
}