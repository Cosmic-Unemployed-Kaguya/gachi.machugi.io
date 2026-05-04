package kaguya.domain.user.model.dto.response;

public record LoginRes(
        String accessToken,
        String refreshToken,

        String nickname  // 화면에띄워줄 닉네임
) {}