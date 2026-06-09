package kaguya.user.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // 400 Bad Request
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "400_INVALID_INPUT_VALUE", "입력값이 올바르지 않습니다."),
    INVALID_CURRENT_PASSWORD(HttpStatus.BAD_REQUEST, "400_INVALID_CURRENT_PASSWORD", "현재 비밀번호와 일치하지 않습니다."),
    SAME_AS_OLD_PASSWORD(HttpStatus.BAD_REQUEST, "400_SAME_AS_OLD_PASSWORD", "이전 비밀번호와 같습니다."),

    // 401 Unauthorized
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "401_INVALID_TOKEN", "토큰이 유효하지 않습니다."),  // (Front) 아무런 동작 안함 or 로그인 페이지로
    MISSING_TOKEN(HttpStatus.UNAUTHORIZED, "401_MISSING_TOKEN", "인증이 필요합니다."),  // (Front) 로그인 페이지로
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "401_EXPIRED_ACCESS_TOKEN", "접근 토큰이 만료되었습니다."),  // (Front) 갱신토큰으로 재시도
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "401_EXPIRED_REFRESH_TOKEN", "갱신 토큰이 만료되었습니다."),  // (Front) 로그인 페이지로
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "401_INVALID_CREDENTIALS", "아이디 또는 비밀번호가 일치하지 않습니다."),

    // 403 Forbidden
    DENIED_PERMISSION(HttpStatus.FORBIDDEN, "403_DENIED_PERMISSION", "접근 권한이 없습니다."),
    ADMIN_ONLY(HttpStatus.FORBIDDEN, "403_ADMIN_ONLY", "접근 권한이 없습니다. (관리자 전용)"),

    // 404 Not Found
    PAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "404_PAGE_NOT_FOUND", "페이지를 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "404_USER_NOT_FOUND", "유저를 찾을 수 없습니다."),

    // 409 Conflict
    EXISTS_USERNAME(HttpStatus.CONFLICT, "409_EXISTS_USERNAME", "이미 사용 중인 아이디입니다."),
    EXISTS_EMAIL(HttpStatus.CONFLICT, "409_EXISTS_EMAIL", "이미 사용 중인 이메일입니다."),
    EXISTS_NICKNAME(HttpStatus.CONFLICT, "409_EXISTS_NICKNAME", "이미 사용 중인 닉네임입니다."),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "500_INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;  // 프론트와 맞춰야 할 에러 코드 (일단 임의로 설정)
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
}
