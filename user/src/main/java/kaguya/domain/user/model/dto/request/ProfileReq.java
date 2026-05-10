package kaguya.domain.user.model.dto.request;

import java.time.LocalDate;

public record ProfileReq(
        String name,
        LocalDate birth,
        String phone,
        String gender
) {}
