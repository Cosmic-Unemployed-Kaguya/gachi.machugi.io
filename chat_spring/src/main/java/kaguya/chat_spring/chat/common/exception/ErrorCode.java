package kaguya.chat_spring.chat.common.exception;

public enum ErrorCode {
    // 권한부족
    DENIED_PERMISSION(403, "403_DENIED_PERMISSION", "권한이 없습니다."),
    ADMIN_ONLY(403, "403_ADMIN_ONLY", "권한이 없습니다. (관리자 전용)"),

    // 서버 내부 오류
    INTERNAL_SERVER_ERROR(500, "500_INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");

    private final int status;
    private final String code;
    private final String message;

    ErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public int getStatus() { return status; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
}
