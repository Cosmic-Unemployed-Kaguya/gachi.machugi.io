package kaguya.user.user.model.dto.request;

import kaguya.user.user.model.dto.AccountDTO;
import kaguya.user.user.model.dto.UserDTO;

public record RegisterReq(
        AccountDTO account,
        UserDTO user
) {}
