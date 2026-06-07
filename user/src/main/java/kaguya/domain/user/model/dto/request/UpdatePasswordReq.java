package kaguya.domain.user.model.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdatePasswordReq(
        @NotBlank(message = "현재 비밀번호를 입력하세요.")
        String currentPassword,

        @NotBlank(message = "새로운 비밀번호를 입력하세요.")
        String newPassword
) {}