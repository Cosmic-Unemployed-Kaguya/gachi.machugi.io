package kaguya.domain.user.model.dto;

import java.time.LocalDate;

public record UserDto(
        String name,
        LocalDate birth,
        String phone,
        String gender
) {}
