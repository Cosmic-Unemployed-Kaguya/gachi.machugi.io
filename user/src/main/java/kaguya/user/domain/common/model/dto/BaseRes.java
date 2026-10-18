package kaguya.user.domain.common.model.dto;

public record BaseRes<T> (
        String code,
        String message,
        T data
) {}
