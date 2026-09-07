package kaguya.user.domain.verification.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kaguya.user.domain.verification.model.enums.VerificationType;

public record SendVerificationCodeReq(

        @NotNull(message = "인증타입은 필수입니다.")
        VerificationType verificationType,

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email
) {}