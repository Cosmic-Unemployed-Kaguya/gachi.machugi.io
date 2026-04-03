package kaguya.user.model.dto.request;

import kaguya.user.model.dto.AccountDTO;
import kaguya.user.model.dto.UserDTO;

public record RegisterReq(
        AccountDTO account,
        UserDTO user
) {}
