package kaguya.domain.user.model.dto.request;

public record ModifyPasswordReq (
        String currentPassword,
        String newPassword
) {}
