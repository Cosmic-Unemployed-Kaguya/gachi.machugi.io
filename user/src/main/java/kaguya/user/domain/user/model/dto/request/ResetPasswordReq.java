package kaguya.user.domain.user.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResetPasswordReq(
        @NotBlank(message = "인증 토큰이 필요합니다.")
        String oneTimeAuthCode,

        @NotBlank(message = "새 비밀번호를 입력해주세요.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
                message = "비밀번호는 8~20자리이며, 영문, 숫자, 특수문자를 포함해야 합니다.")
        String newPassword
) {}