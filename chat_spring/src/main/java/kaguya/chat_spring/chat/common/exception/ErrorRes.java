package kaguya.chat_spring.chat.common.exception;

public record ErrorRes(
        int status,
        String code,
        String message
) {
    public ErrorRes(ErrorCode errorCode) {
        this (
            errorCode.getStatus(),
            errorCode.getCode(),
            errorCode.getMessage()
        );
    }
}