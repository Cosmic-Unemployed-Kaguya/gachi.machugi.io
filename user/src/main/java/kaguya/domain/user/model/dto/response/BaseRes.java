package kaguya.domain.user.model.dto.response;

public record BaseRes<T> (
        String code,
        String message,
        T data
) {}
