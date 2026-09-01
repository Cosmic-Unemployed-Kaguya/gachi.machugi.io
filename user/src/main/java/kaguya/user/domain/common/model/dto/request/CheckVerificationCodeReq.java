package kaguya.user.domain.common.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kaguya.user.domain.common.model.enums.VerificationType;

public record CheckVerificationCodeReq(

        @NotNull
        VerificationType verificationType,

        @NotBlank(message = "인증코드는 필수입니다.")
        String verificationCode,

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email
) {}