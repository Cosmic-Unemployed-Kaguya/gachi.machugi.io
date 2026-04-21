package kaguya.domain.user.controller;

import kaguya.domain.user.model.dto.request.LoginReq;
import kaguya.domain.user.model.dto.request.RegisterReq;
import kaguya.domain.user.model.dto.response.BaseRes;
import kaguya.domain.user.model.dto.response.TokenRes;
import kaguya.domain.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입 (gRPC)
     * @param request: 회원가입 요청 DTO (계정 정보, 사용자 정보)
     * @return BaseRes<void>: HTTP 201 생성
     */
    @PostMapping("/register")
    public ResponseEntity<BaseRes<Void>> register(@RequestBody RegisterReq request) {

        authService.register(request);
        BaseRes<Void> response = new BaseRes<>("201", "회원가입 성공", null);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 로그인
     * @param request: 로그인 요청 DTO (Id/Password)
     * @return BaseRes<String>: HTTP 200 성공, nickname 전달
     */
    @PostMapping("/login")
    public ResponseEntity<BaseRes<String>> login(@RequestBody LoginReq request) {

        TokenRes data = authService.login(request);

        // Access Cookie 설정 (key: accessToken, value: AccessToken)
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", data.accessToken())
                .httpOnly(true)  // JavaScript로 쿠키 접근 차단
                .secure(false)  // 로컬 테스트용 (나중에 HTTPS 서버 배포 시 반드시 true로)
                .path("/")  // 서비스의 모든 URL에서 이 쿠키를 사용
                .maxAge(10 * 60)  // 10분
                .sameSite("Lax")  // CSRF 공격 방어를 위한 설정 (Lax or Strict)
                .build();

        // Refresh Cookie 설정 (key: refreshToken, value: RefreshToken)
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", data.refreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(14 * 24 * 60 * 60) // 14일
                .sameSite("Lax")
                .build();

        // cookie 세팅
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new BaseRes<>("200", "로그인 성공", data.nickname()));  // 닉네임만 전달
    }

    /**
     * 로그아웃
     * @param accessToken: 접근 토큰
     * @param refreshToken: 갱신 토큰
     * @return <BaseRes<void>: HTTP 200 성공
     */
    @PostMapping("/logout")
    public ResponseEntity<BaseRes<Void>> logout(
            @CookieValue(name = "accessToken", required = false) String accessToken,
            @CookieValue(name = "refreshToken", required = false) String refreshToken
    ) {

         authService.logout(accessToken, refreshToken);

        // Access Token 쿠키 삭제
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)  // 0으로 설정 (브라우저에서 삭제)
                .sameSite("Lax")
                .build();

        // Refresh Token 쿠키 삭제
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new BaseRes<>("200", "로그아웃 성공", null));
    }

    /**
     * 토큰 검증 (gRPC)
     * @param accessToken: 접근 토큰
     * @return <BaseRes<void>: HTTP 200 성공
     */
    @PostMapping("/checkToken")
    public ResponseEntity<BaseRes<String>> checkToken (
            @CookieValue("accessToken") String accessToken
    ) {

        String id = authService.checkToken(accessToken);

        return ResponseEntity.ok()
                .body(new BaseRes<>("200", "인증 성공", id));
    }

    /**
     * 토큰 갱신
     * @param refreshToken: 갱신 토큰
     * @return <BaseRes<void>: HTTP 200 성공
     */
    @PostMapping("/reissue")
    public ResponseEntity<BaseRes<Void>> renewToken (
            @CookieValue(name = "refreshToken") String refreshToken
    ) {

        String access = authService.renewToken(refreshToken);

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", access)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(10 * 60)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .body(new BaseRes<>("200", "토큰 갱신", null));
    }
}