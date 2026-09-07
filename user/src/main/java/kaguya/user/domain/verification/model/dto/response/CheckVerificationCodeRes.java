package kaguya.user.domain.verification.model.dto.response;

public record CheckVerificationCodeRes(
        String oneTimeAuthCode
) {}
