package kaguya.user.domain.auth.model.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterReq(

        @NotBlank(message = "인증 토큰이 필요합니다.")
        String oneTimeAuthCode,

        @Valid
        @NotNull(message = "계정 정보를 입력하세요.")
        AccountReq account,

        @Valid
        @NotNull(message = "유저 정보를 입력하세요.")
        UserReq user
) {}