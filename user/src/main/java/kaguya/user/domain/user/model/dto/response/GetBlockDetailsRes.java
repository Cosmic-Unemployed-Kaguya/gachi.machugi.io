package kaguya.user.domain.user.model.dto.response;

import java.time.LocalDateTime;

public record GetBlockDetailsRes (
        Long idx,
        Long userIdx,
        String userEmail,
        String userNickname,
        String managementType,
        String reason,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String adminNickname
) {}