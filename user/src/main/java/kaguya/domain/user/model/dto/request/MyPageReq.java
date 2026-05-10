package kaguya.domain.user.model.dto.request;

public record MyPageReq(
        String username,
        String nickname,
        String email
) {}
