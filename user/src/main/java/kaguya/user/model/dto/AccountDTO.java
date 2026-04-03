package kaguya.user.model.dto;

public record AccountDTO (
        String id,
        String password,
        String nickname,
        String email
) {}
