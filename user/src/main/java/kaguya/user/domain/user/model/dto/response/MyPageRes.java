package kaguya.user.domain.user.model.dto.response;

import java.time.LocalDateTime;

public record MyPageRes(
//        String username,
        String email,
        String nickname,
        LocalDateTime joinDate
) {}
