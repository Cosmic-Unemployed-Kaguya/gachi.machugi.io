package kaguya.domain.user.model.dto.request;

public record AccountReq(
        String username,
        String password,
        String email,
        String nickname
) {}