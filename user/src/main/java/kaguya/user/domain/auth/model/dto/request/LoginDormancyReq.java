package kaguya.user.domain.auth.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginDormancyReq(

        @NotBlank(message = "인증 토큰이 필요합니다.")
        String oneTimeAuthCode,

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Size(max = 100)
        String password
) {}
