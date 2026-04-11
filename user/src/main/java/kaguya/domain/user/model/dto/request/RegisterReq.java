package kaguya.domain.user.model.dto.request;

import kaguya.domain.user.model.dto.AccountDTO;
import kaguya.domain.user.model.dto.UserDTO;

public record RegisterReq(
        AccountDTO account,
        UserDTO user
) {}
