package kaguya.user.domain.user.model.dto.response;

public record MyPageRes(
        String username,
        String email,
        String nickname
) {}
