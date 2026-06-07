package kaguya.domain.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UserDto(
        @NotBlank(message = "성함는 필수입니다.")
        String name,

        @NotNull(message = "생년월일은 필수입니다.")
        LocalDate birth,

        @NotBlank(message = "연락처는 필수입니다.")
        String phone,

        @NotBlank(message = "성별을 선택해주세요. (NONE / MALE / FEMALE)")
        String gender
) {}