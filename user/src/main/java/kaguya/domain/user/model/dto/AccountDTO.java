package kaguya.domain.user.model.dto;

public record AccountDTO (
        String username,
        String password,
        String nickname,
        String email
) {}
