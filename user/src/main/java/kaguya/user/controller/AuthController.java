package kaguya.user.controller;

import kaguya.user.model.dto.response.BaseRes;
import kaguya.user.model.dto.response.RegisterRes;
import kaguya.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<BaseRes<RegisterRes>> register() {

        RegisterRes data = authService.register();
        return ResponseEntity.ok(
                new BaseRes<>("200", "회원가입 성공", data)
        );
    }

}
