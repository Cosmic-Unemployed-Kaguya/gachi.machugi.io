package kaguya.user.user.controller;

import kaguya.user.user.model.dto.response.BaseRes;
import kaguya.user.user.model.dto.response.RegisterRes;
import kaguya.user.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<BaseRes<RegisterRes>> register() {

        RegisterRes data = userService.register();
        return ResponseEntity.ok(
                new BaseRes<>("200", "회원가입 성공", data)
        );
    }

}
