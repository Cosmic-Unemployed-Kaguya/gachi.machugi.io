package kaguya.domain.user.model.dto.response;

public record CheckTokenRes (
        String username,
        String role
) {}
