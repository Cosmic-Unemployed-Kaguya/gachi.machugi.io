package kaguya.domain.user.model.dto.request;

import kaguya.domain.user.model.dto.AccountInfo;
import kaguya.domain.user.model.dto.UserInfo;

public record RegisterReq(
        AccountInfo account,
        UserInfo user
) {}
