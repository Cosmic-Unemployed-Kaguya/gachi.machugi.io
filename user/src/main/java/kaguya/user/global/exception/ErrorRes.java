package kaguya.user.global.exception;

public record ErrorRes(
        int status,
        String code,
        String message
) {
    public ErrorRes(ErrorCode errorCode) {
        this(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage()
        );
    }
}