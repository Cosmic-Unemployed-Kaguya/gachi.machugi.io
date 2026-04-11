package kaguya.domain.user.controller;

import kaguya.domain.user.model.dto.request.LoginReq;
import kaguya.domain.user.model.dto.request.RegisterReq;
import kaguya.domain.user.model.dto.response.BaseRes;
import kaguya.domain.user.model.dto.response.LoginRes;
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

    @PostMapping("/register")
    public ResponseEntity<BaseRes<Void>> register(@RequestBody RegisterReq request) {

        authService.register(request);
        BaseRes<Void> response = new BaseRes<>("201", "회원가입 성공", null);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<BaseRes<String>> login(@RequestBody LoginReq request) {

        LoginRes data = authService.login(request);

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", data.accessToken())
                .httpOnly(true)
                .secure(false) // 로컬 테스트용 (나중에 HTTPS 서버 배포 시 반드시 true로)
                .path("/")
                .maxAge(10 * 60) // 10분
                .sameSite("Lax") // CSRF 공격 방어를 위한 설정 (Lax or Strict)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", data.refreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(14 * 24 * 60 * 60) // 14일
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new BaseRes<>("200", "로그인 성공", data.nickname()));  // 닉네임만 전달
    }
}