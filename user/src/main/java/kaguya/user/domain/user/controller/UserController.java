package kaguya.user.domain.user.controller;

import jakarta.validation.Valid;
import kaguya.user.domain.common.model.dto.BaseRes;
import kaguya.user.domain.user.model.dto.request.UpdateNicknameReq;
import kaguya.user.domain.user.model.dto.request.UpdatePasswordReq;
import kaguya.user.domain.user.model.dto.response.MyPageRes;
import kaguya.user.domain.user.model.dto.response.ProfileReq;
import kaguya.user.domain.user.service.UserService;
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
    public ResponseEntity<BaseRes<ProfileReq>> getProfile(
            @RequestHeader("x-user-id") String username
    ) {

        ProfileReq data = userService.getProfile(username);

        BaseRes<ProfileReq> response = new BaseRes<>("200", "프로필 조회", data);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/my/password")
    public ResponseEntity<BaseRes<Void>> updatePasswords(
            @RequestHeader("x-user-id") String username,
            @RequestBody @Valid UpdatePasswordReq request
    ) {

        userService.updatePassword(username, request);

        BaseRes<Void> response = new BaseRes<>("200", "비밀번호 수정 완료", null);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/my/nickname")
    public ResponseEntity<BaseRes<Void>> updateNickname(
            @RequestHeader("x-user-id") String username,
            @RequestBody @Valid UpdateNicknameReq request
    ) {

        userService.updateNickname(username, request);

        BaseRes<Void> response = new BaseRes<>("200", "닉네임 수정 완료", null);
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