package kaguya.user.controller;

import kaguya.user.model.dto.request.RegisterReq;
import kaguya.user.model.dto.response.BaseRes;
import kaguya.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}