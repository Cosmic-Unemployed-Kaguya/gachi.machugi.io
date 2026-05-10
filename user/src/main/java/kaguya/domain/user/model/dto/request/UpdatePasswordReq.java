package kaguya.domain.user.model.dto.request;

public record UpdatePasswordReq(
        String currentPassword,
        String newPassword
) {}
