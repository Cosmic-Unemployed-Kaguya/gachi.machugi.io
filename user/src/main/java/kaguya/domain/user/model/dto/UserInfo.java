package kaguya.domain.user.model.dto;

import java.time.LocalDate;

public record UserInfo(
        String name,
        LocalDate birth,
        String phone,
        String gender
) {}
