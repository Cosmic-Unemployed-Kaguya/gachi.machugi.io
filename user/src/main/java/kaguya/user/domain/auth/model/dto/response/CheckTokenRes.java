package kaguya.user.domain.auth.model.dto.response;

public record CheckTokenRes (
        String username,
        String role
) {}
