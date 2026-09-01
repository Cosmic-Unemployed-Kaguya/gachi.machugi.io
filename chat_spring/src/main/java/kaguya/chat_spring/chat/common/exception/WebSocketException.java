package kaguya.chat_spring.chat.common.exception;

public class WebSocketException extends RuntimeException {

    private final ErrorCode errorCode;

    public WebSocketException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() { return errorCode; }
}
