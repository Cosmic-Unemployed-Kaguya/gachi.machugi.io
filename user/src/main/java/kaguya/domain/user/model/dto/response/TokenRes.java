package kaguya.domain.user.model.dto.response;

public record TokenRes(
        String accessToken,
        String refreshToken,

        String nickname
) {}