package kaguya.user.domain.user.model.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateNicknameReq(
        @NotBlank(message = "닉네임을 입력하세요.")
        String nickname
) {}