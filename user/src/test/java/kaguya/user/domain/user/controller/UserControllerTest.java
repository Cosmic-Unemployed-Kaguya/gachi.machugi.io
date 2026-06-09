package kaguya.user.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kaguya.user.domain.common.model.enums.Gender;
import kaguya.user.domain.user.model.dto.request.UpdateNicknameReq;
import kaguya.user.domain.user.model.dto.request.UpdatePasswordReq;
import kaguya.user.domain.user.model.dto.response.MyPageRes;
import kaguya.user.domain.user.model.dto.response.ProfileReq;
import kaguya.user.domain.user.model.entity.UserEntity;
import kaguya.user.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)  // spring security 필터 무시
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UserService userService;

    /**
     * 정상 테스트 (Happy Path)
     */
    @Test
    @DisplayName("마이페이지 조회 성공")
    void 마이페이지_성공 () throws Exception {

        UserEntity user = createUser();

        MyPageRes myPage = new MyPageRes(
                user.getUsername(),
                user.getEmail(),
                user.getNickname()
        );

        given(userService.getMyPage(user.getUsername())).willReturn(myPage);

        mockMvc.perform(get("/users/my")
                        .header("X-User-Id", user.getUsername()))

                // 응답 검증
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("마이페이지 조회"))
                // 데이터
                .andExpect(jsonPath("$.data.username").value("testID"))
                .andExpect(jsonPath("$.data.email").value("aaaa@bbbb.com"))
                .andExpect(jsonPath("$.data.nickname").value("user1"));
    }

    @Test
    @DisplayName("프로필 조회 성공")
    void 프로필_성공() throws Exception {

        UserEntity user = createUser();
        ProfileReq response = new ProfileReq(
                user.getName(),
                user.getBirth(),
                user.getPhone(),
                user.getGender().toString()
        );

        given(userService.getProfile(user.getUsername())).willReturn(response);

        mockMvc.perform(get("/users/my/profile")
                        .header("X-User-Id", user.getUsername()))

                // 응답 검증
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("프로필 조회"))
                // 데이터
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andExpect(jsonPath("$.data.birth").value(user.getBirth().toString()))
                .andExpect(jsonPath("$.data.phone").value("010-1234-5678"))
                .andExpect(jsonPath("$.data.gender").value(Gender.MALE.toString()));
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void 비밀번호_번경_성공() throws Exception {

        UserEntity user = createUser();
        UpdatePasswordReq request = new UpdatePasswordReq(
                "encodedPassword123",
                "changedPassword123!@"
        );

        // 비밀번호 변경 return이 null이어서 given 의미 없음

        mockMvc.perform(patch("/users/my/password")
                        .header("X-User-Id", user.getUsername())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // 응답 검증
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("비밀번호 수정 완료"));
    }

    @Test
    @DisplayName("닉네임 변경 성공")
    void 닉네임_번경_성공() throws Exception {

        UserEntity user = createUser();
        UpdateNicknameReq request = new UpdateNicknameReq(
                user.getNickname()
        );

        // 닉네임 변경 return이 null이어서 given 의미 없음

        mockMvc.perform(patch("/users/my/nickname")
                        .header("X-User-Id", user.getUsername())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // 응답 검증
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("닉네임 수정 완료"));
    }

    @Test
    @DisplayName("회원탈퇴")
    void 회원탈퇴() throws Exception {

        UserEntity user = createUser();

        mockMvc.perform(delete("/users/my/withdraw")
                        .header("X-User-Id", user.getUsername()))

                // 응답 검증
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("회원 탈퇴"));
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