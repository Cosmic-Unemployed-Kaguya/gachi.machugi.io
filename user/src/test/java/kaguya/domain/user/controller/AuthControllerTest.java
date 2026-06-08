package kaguya.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import kaguya.domain.user.model.dto.UserDto;
import kaguya.domain.user.model.dto.request.AccountReq;
import kaguya.domain.user.model.dto.request.LoginReq;
import kaguya.domain.user.model.dto.request.RegisterReq;
import kaguya.domain.user.model.dto.response.LoginRes;
import kaguya.domain.user.model.enums.Gender;
import kaguya.domain.user.service.AuthService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)  // spring security 필터 무시
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean AuthService authService;

    /**
     * 정상 테스트 (Happy Path)
     */
    @Test
    @DisplayName("회원가입 성공")
    void 회원가입_테스트_성공() throws Exception {
        AccountReq account = new AccountReq("testID", "testPassword", "user1", "aaaa@bbbb.com");
        UserDto user = new UserDto("홍길동", LocalDate.now(), "010-1234-5678", Gender.MALE.toString());
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

        given(authService.login(any(LoginReq.class))).willReturn(response);

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

        given(authService.renewToken(any(String.class))).willReturn(accessToken);

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
    // todo. 비정상 테스트
}