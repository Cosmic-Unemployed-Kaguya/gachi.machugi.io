package kaguya.domain.user.model.dto.request;

public record LoginReq (
        String username,
        String password
) {}
