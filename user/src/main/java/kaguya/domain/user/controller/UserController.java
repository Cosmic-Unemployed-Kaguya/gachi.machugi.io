package kaguya.domain.user.controller;

import kaguya.domain.user.model.dto.response.MyPageRes;
import kaguya.domain.user.model.dto.response.ProfileRes;
import kaguya.domain.user.model.dto.request.UpdateNicknameReq;
import kaguya.domain.user.model.dto.request.UpdatePasswordReq;
import kaguya.domain.user.model.dto.response.BaseRes;
import kaguya.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/my")
    public ResponseEntity<BaseRes<MyPageRes>> getMyPage(
            @RequestHeader(value = "x-user-id") String username
    ) {

        MyPageRes data = userService.getMyPage(username);

        BaseRes<MyPageRes> response = new BaseRes<>("200", "마이페이지 조회", data);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my/profile")
    public ResponseEntity<BaseRes<ProfileRes>> getProfile(
            @RequestHeader("x-user-id") String username
    ) {

        ProfileRes data = userService.getProfile(username);

        BaseRes<ProfileRes> response = new BaseRes<>("200", "프로필 조회", data);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/my/nickname")
    public ResponseEntity<BaseRes<Void>> updateNickname(
            @RequestHeader("x-user-id") String username,
            @RequestBody UpdateNicknameReq request
    ) {

        userService.updateNickname(username, request);

        BaseRes<Void> response = new BaseRes<>("200", "닉네임 수정 완료", null);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/my/password")
    public ResponseEntity<BaseRes<Void>> updatePasswords(
            @RequestHeader("x-user-id") String username,
            @RequestBody UpdatePasswordReq request
    ) {

        userService.updatePassword(username, request);

        BaseRes<Void> response = new BaseRes<>("200", "비밀번호 수정 완료", null);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/my/withdraw")
    public ResponseEntity<BaseRes<Void>> withdraw(
            @RequestHeader("x-user-id") String username
    ) {

        userService.withdraw(username);

        BaseRes<Void> response = new BaseRes<>("200", "회원 탈퇴", null);
        return ResponseEntity.ok(response);
    }
}