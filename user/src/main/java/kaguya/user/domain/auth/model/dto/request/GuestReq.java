package kaguya.user.domain.auth.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record GuestReq(
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하로 입력해주세요.")
        @Pattern(regexp = "^[a-zA-Z0-9가-힣]+$", message = "닉네임은 특수문자와 공백을 포함할 수 없습니다.")
        String nickname
) {}
