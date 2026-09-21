package kaguya.user.domain.user.model.dto.response;

import java.util.List;

public record GetUsersInfoReq (
        List<GetUserDetailsReq> userList
) {}