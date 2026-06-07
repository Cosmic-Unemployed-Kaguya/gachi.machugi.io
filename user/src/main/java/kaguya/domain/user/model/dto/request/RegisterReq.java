package kaguya.domain.user.model.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import kaguya.domain.user.model.dto.UserDto;

public record RegisterReq(
        @Valid
        @NotNull(message = "계정 정보를 입력하세요.")
        AccountReq account,

        @Valid
        @NotNull(message = "유저 정보를 입력하세요.")
        UserDto user
) {}
