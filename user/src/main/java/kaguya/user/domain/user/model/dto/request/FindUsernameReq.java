package kaguya.user.domain.user.model.dto.request;

import jakarta.validation.constraints.NotBlank;

public record FindUsernameReq (
        @NotBlank(message = "인증 토큰이 필요합니다.")
        String oneTimeAuthCode
) {}
