package kaguya.domain.user.model.dto.request;

import kaguya.domain.user.model.dto.UserDto;

public record RegisterReq(
        AccountReq account,
        UserDto user
) {}
