package kaguya.user.domain.user.model.dto.response;

import java.util.List;

public record GetUsersInfoRes(
        List<GetUserDetailsRes> userList,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {}