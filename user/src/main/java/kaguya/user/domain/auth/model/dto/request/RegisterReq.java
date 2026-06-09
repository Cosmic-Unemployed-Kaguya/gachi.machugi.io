package kaguya.user.domain.auth.model.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record RegisterReq(
        @Valid
        @NotNull(message = "계정 정보를 입력하세요.")
        AccountReq account,

        @Valid
        @NotNull(message = "유저 정보를 입력하세요.")
        UserReq user
) {}
