package kaguya.user.domain.user.model.dto.response;

import java.time.LocalDateTime;

public record GetUserDetailsReq(
        Long idx,
        String email,
        String nickname,
        Long point,
        String role,
        String status,
        LocalDateTime joinDate,
        LocalDateTime lastLoginDate,
        LocalDateTime withdrawalDate

        // 유저 개인정보는 관리자가 알 필요 없음
) {}