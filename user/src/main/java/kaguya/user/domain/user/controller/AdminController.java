package kaguya.user.domain.user.controller;

import jakarta.validation.Valid;
import kaguya.user.domain.common.model.dto.BaseRes;
import kaguya.user.domain.user.model.dto.request.BlockReq;
import kaguya.user.domain.user.model.dto.request.UpdateNicknameReq;
import kaguya.user.domain.user.model.dto.request.UpdateRoleReq;
import kaguya.user.domain.user.model.dto.request.UserSearchReq;
import kaguya.user.domain.user.model.dto.response.GetBlocksInfoRes;
import kaguya.user.domain.user.model.dto.response.GetUserDetailsRes;
import kaguya.user.domain.user.model.dto.response.GetUsersInfoRes;
import kaguya.user.domain.user.model.enums.Role;
import kaguya.user.domain.user.service.AdminService;
import kaguya.user.global.exception.BusinessException;
import kaguya.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    // 정렬할 필드의 화이트리스트
    private static final Set<String> SORTABLE_PROPERTIES = Set.of(
            "joinDate",
            "lastLoginDate"
            // 필요하면 추가
    );

    private final AdminService adminService;

    @GetMapping("/users/info")
    public ResponseEntity<BaseRes<GetUsersInfoRes>> getUserList(
            @RequestHeader(value = "x-user-idx", required = false) Long idx,
            @RequestHeader(value = "x-user-role", required = false) String role,
            @ModelAttribute UserSearchReq condition,
            @PageableDefault(size = 20, sort = "joinDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {

        if (!Role.ADMIN.name().equals(role) || idx == null) {
            throw new BusinessException(ErrorCode.DENIED_PERMISSION);
        }

        GetUsersInfoRes data = adminService.getUserList(condition, validateSort(pageable));

        return ResponseEntity.ok(
                new BaseRes<>("200", "유저 리스트 조회", data)
        );
    }

    @GetMapping("/users/{userIdx}")
    public ResponseEntity<BaseRes<GetUserDetailsRes>> getUserDetails(
            @RequestHeader(value = "x-user-idx", required = false) Long idx,
            @RequestHeader(value = "x-user-role", required = false) String role,
            @PathVariable("userIdx") Long userIdx
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
            @RequestHeader(value = "x-user-idx", required = false) Long idx,
            @RequestHeader(value = "x-user-role", required = false) String role,
            @PathVariable("userIdx") Long userIdx,
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
            @RequestHeader(value = "x-user-idx", required = false) Long idx,
            @RequestHeader(value = "x-user-role", required = false) String role,
            @PathVariable("userIdx") Long userIdx,
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
            @RequestHeader(value = "x-user-idx", required = false) Long adminIdx,
            @RequestHeader(value = "x-user-role", required = false) String role,
            @PathVariable("userIdx") Long userIdx,
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

    @GetMapping("/users/block/list")
    public ResponseEntity<BaseRes<GetBlocksInfoRes>> getBlockList(
            @RequestHeader(value = "x-user-idx", required = false) Long idx,
            @RequestHeader(value = "x-user-role", required = false) String role
    ) {

        if (!Role.ADMIN.name().equals(role) || idx == null) {
            throw new BusinessException(ErrorCode.DENIED_PERMISSION);
        }

        GetBlocksInfoRes data = adminService.getBlockList();

        return ResponseEntity.ok(
                new BaseRes<>("200", "유저 제제 리스트 조회", data)
        );
    }

    // 정렬 항목 검사 (화이트리스트 기준)
    private static Pageable validateSort(Pageable pageable) {

        List<Sort.Order> orders = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (Sort.Order order : pageable.getSort()) {
            String property = order.getProperty();

            // 화이트리스트(SORTABLE_PROPERTIES)에 포함되어있는지 검사
            if (!SORTABLE_PROPERTIES.contains(property)) {
                // todo. 커스텀 exception으로 변경
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "정렬할 수 없는 필드: " + property
                );
            }
            // 중복 확인
            if (!seen.add(property)) {
                // todo. 커스텀 exception으로 변경
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "정렬 필드 중복: " + property
                );
            }

            // 검사 통과 -> NULLS_LAST 설정을 붙여 추가
            orders.add(order.nullsLast());
        }

        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(orders)
        );
    }
}