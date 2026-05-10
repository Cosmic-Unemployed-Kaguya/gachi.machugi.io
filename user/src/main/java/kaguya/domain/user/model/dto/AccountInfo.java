package kaguya.domain.user.model.dto;

public record AccountInfo(
        String username,
        String password,
        String nickname,
        String email
) {}
