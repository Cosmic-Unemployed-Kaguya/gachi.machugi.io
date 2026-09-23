package kaguya.user.domain.user.controller;

import jakarta.validation.Valid;
import kaguya.user.domain.common.model.dto.BaseRes;
import kaguya.user.domain.user.model.dto.request.BlockReq;
import kaguya.user.domain.user.model.dto.request.UpdateNicknameReq;
import kaguya.user.domain.user.model.dto.request.UpdateRoleReq;
import kaguya.user.domain.user.model.dto.response.GetUserDetailsRes;
import kaguya.user.domain.user.model.dto.response.GetUsersInfoRes;
import kaguya.user.domain.user.model.enums.Role;
import kaguya.user.domain.user.service.AdminService;
import kaguya.user.global.exception.BusinessException;
import kaguya.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    // todo. 페이징 처리
    @GetMapping("/users/info")
    public ResponseEntity<BaseRes<GetUsersInfoRes>> getUserList(
            @RequestHeader(value = "x-user-id", required = false) Long idx,
            @RequestHeader(value = "x-user-role", required = false) String role
    ) {

        if (!Role.ADMIN.name().equals(role) || idx == null) {
            throw new BusinessException(ErrorCode.DENIED_PERMISSION);
        }

        GetUsersInfoRes data = adminService.getUserList();

        return ResponseEntity.ok(
                new BaseRes<>("200", "유저 리스트 조회", data)
        );
    }

    @GetMapping("/users/{userIdx}")
    public ResponseEntity<BaseRes<GetUserDetailsRes>> getUserDetails(
            @RequestHeader(value = "x-user-id", required = false) Long idx,
            @RequestHeader(value = "x-user-role", required = false) String role,
            @RequestParam("userIdx") Long userIdx
    ) {

        if (!Role.ADMIN.name().equals(role) || idx == null) {
            throw new BusinessException(ErrorCode.DENIED_PERMISSION);
        }

        GetUserDetailsRes data = adminService.getUserDetails(userIdx);

        return ResponseEntity.ok(
                new BaseRes<>("200", "유저 정보 조회", data)
        );
    }

    @PatchMapping("/users/{userIdx}/nickname")
    public ResponseEntity<BaseRes<Void>> updateNickname(
            @RequestHeader(value = "x-user-id", required = false) Long idx,
            @RequestHeader(value = "x-user-role", required = false) String role,
            @RequestParam("userIdx") Long userIdx,
            @RequestBody @Valid UpdateNicknameReq request
    ) {

        if (!Role.ADMIN.name().equals(role) || idx == null) {
            throw new BusinessException(ErrorCode.DENIED_PERMISSION);
        }

        adminService.updateNickname(userIdx, request.nickname());

        return ResponseEntity.ok(
                new BaseRes<>("200", "닉네임 수정 완료", null)
        );
    }

    @PatchMapping("/users/{userIdx}/role")
    public ResponseEntity<BaseRes<Void>> updateRole(
            @RequestHeader(value = "x-user-id", required = false) Long idx,
            @RequestHeader(value = "x-user-role", required = false) String role,
            @RequestParam("userIdx") Long userIdx,
            @RequestBody @Valid UpdateRoleReq request
    ) {

        if (!Role.ADMIN.name().equals(role) || idx == null) {
            throw new BusinessException(ErrorCode.DENIED_PERMISSION);
        }

        adminService.updateRole(userIdx, request.role());

        return ResponseEntity.ok(
                new BaseRes<>("200", "권한 수정 완료", null)
        );
    }

    @PostMapping("/users/{userIdx}/block")
    public ResponseEntity<BaseRes<Void>> blockUser(
            @RequestHeader(value = "x-user-id", required = false) Long adminIdx,
            @RequestHeader(value = "x-user-role", required = false) String role,
            @RequestParam("userIdx") Long userIdx,
            @RequestBody @Valid BlockReq request
    ) {

        if (!Role.ADMIN.name().equals(role) || adminIdx == null) {
            throw new BusinessException(ErrorCode.DENIED_PERMISSION);
        }

        adminService.blockUser(userIdx, adminIdx, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new BaseRes<>("201", "유저 차단", null)
        );
    }
}