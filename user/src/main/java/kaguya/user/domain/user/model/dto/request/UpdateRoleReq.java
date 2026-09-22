package kaguya.user.domain.user.model.dto.request;

import jakarta.validation.constraints.NotNull;
import kaguya.user.domain.user.model.enums.Role;

public record UpdateRoleReq(
        @NotNull(message = "바꿀 권한을 입력하세요.")
        Role role
) {}
