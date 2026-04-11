package kaguya.domain.user.model.dto.response;

public record LoginRes(
        String accessToken,
        String refreshToken,

        String nickname
) {}