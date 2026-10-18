package kaguya.user.domain.user.model.dto.response;

import java.time.LocalDate;

public record ProfileRes(
        String name,
        LocalDate birth,
        String phone,
        String gender
) {}