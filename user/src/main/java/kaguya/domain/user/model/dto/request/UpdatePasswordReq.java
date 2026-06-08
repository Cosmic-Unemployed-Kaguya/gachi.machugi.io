package kaguya.domain.user.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdatePasswordReq(
        @NotBlank(message = "현재 비밀번호를 입력하세요.")
        String currentPassword,

        @NotBlank(message = "새로운 비밀번호를 입력하세요.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
                message = "비밀번호는 8~20자리이며, 영문, 숫자, 특수문자를 포함해야 합니다.")
        String newPassword
) {}