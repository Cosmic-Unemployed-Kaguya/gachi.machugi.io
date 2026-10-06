package kaguya.user.domain.user.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kaguya.user.domain.user.model.enums.ManagementType;

import java.time.LocalDateTime;

public record BlockReq(

        @NotNull(message = "차단 종류를 선택하세요.")
        ManagementType managementType,

        @NotBlank(message = "차단 이유를 적어주세요.")
        String reason,

        // 임시차단용
        LocalDateTime endDate
) {}