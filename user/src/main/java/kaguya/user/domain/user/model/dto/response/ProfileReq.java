package kaguya.user.domain.user.model.dto.response;

import java.time.LocalDate;

public record ProfileReq(
        String name,
        LocalDate birth,
        String phone,
        String gender
) {}