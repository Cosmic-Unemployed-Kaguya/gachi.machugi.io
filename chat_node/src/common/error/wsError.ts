

export class WsError extends Error{

    public code : string;
    public isOperational: boolean;
    public level: ErrorLevel;

    // message : 외부 출력용 메시지

    // 내부 로깅용 메시지
    public internalMessage?: string;

    private constructor(
        code: string,
        message: string,
        isOperational: boolean = true,
        level: ErrorLevel = ErrorLevel.WARN,
        stack?: string,
        internalMessage?: string,
    ) {
        super(message);
        this.code = code;
        this.isOperational = isOperational;
        this.level = level;
        this.name = "WsError";

        if (stack) {
            this.stack = stack;
        } else {
            Error.captureStackTrace(this, this.constructor);
        }

        if (internalMessage) {
            this.internalMessage = internalMessage;
        }
    }

    public static fromType(
        type: WsErrorType,
        internalMessage?: string,
        level: ErrorLevel = ErrorLevel.WARN
    ): WsError {
        const { code, message } = WsErrorCode[type];
        return new WsError(code, message, true, level, internalMessage);
    }

    public static custom(
        code: string,
        message: string,
        isOperational: boolean = true,
        level: ErrorLevel = ErrorLevel.WARN,
        stack?: string,
        internalMessage?: string,
    ): WsError {
        return new WsError(code, message, true, level, stack, internalMessage);
    }
    
    public static fatal(
        internalMessage: string,
        error?: unknown
    ): WsError {
        const { code, message } = WsErrorCode.INTERNAL_SERVER_ERROR;
        return new WsError(
            code, 
            message, 
            false, 
            ErrorLevel.ERROR, 
            `${internalMessage} - ${error instanceof Error ? error.message : String(error)}`
        );
    }
}


export enum ErrorLevel {
    INFO = "info",
    WARN = "warn",
    ERROR = "error",
}

export const WsErrorCode = {
    // 1. 방(Room) 관련
    ROOM_ALREADY_EXISTS: { code: "ROOM_ALREADY_EXISTS", message: "이미 존재하는 방입니다." },
    ROOM_NOT_FOUND: { code: "ROOM_NOT_FOUND", message: "존재하지 않는 방입니다." },
    ALREADY_IN_ROOM: { code: "ALREADY_IN_ROOM", message: "이미 방에 참여 중입니다." },
    NOT_IN_ROOM: { code: "NOT_IN_ROOM", message: "해당 방에 참여하고 있지 않습니다." },
    ROOM_FULL: { code: "ROOM_FULL", message: "방 인원이 가득 찼습니다." },
    ROOM_LOCKED: { code: "ROOM_LOCKED", message: "비밀번호가 틀렸거나 잠긴 방입니다." },

    // 2. 인증 및 권한
    UNAUTHORIZED_USER: { code: "UNAUTHORIZED_USER", message: "유저 인증에 실패했습니다." },
    FORBIDDEN_ACTION: { code: "FORBIDDEN_ACTION", message: "해당 작업을 수행할 권한이 없습니다." },
    USER_KICKED: { code: "USER_KICKED", message: "방에서 강퇴당하여 재입장할 수 없습니다." },

    // 3. 데이터 유효성 (Zod 등)
    INVALID_PAYLOAD: { code: "INVALID_PAYLOAD", message: "잘못된 데이터 형식입니다." },
    MISSING_REQUIRED_FIELD: { code: "MISSING_REQUIRED_FIELD", message: "필수 항목이 누락되었습니다." },

    NOT_FOUND_EVENT_ERROR: {code: "NOT_FOUND_EVENT_ERROR", message:'존재하지 않는 이벤트입니다.'},
    INVALID_TICKET: { code: "INVALID_TICKET", message: "유효하지 않은 티켓입니다." },
    INVALID_ACCESS: { code: "INVALID_ACCESS", message: "잘못된 접근입니다." },

    INVALID_REQUEST: { code: "INVALID_REQUEST", message: "잘못된 요청입니다." },
    
    // 4. 시스템 및 서버 내부
    INTERNAL_CONNECTION_ERROR: { code: "INTERNAL_CONNECTION_ERROR", message: "내부 통신 중 에러가 발생했습니다." },
    INTERNAL_SERVER_ERROR: { code: "INTERNAL_SERVER_ERROR", message: "서버 내부 오류가 발생했습니다." },
    
    SERVER_EVENT_LOAD_ERROR: {code: "SERVER_EVENT_LOAD_ERROR", message: "서버 이벤트 로드 중 에러가 발생했습니다."},
    SOCKET_EVENT_LOAD_ERROR: {code: "SOCKET_EVENT_LOAD_ERROR", message: "소켓 이벤트 로드 중 에러가 발생했습니다."},
    MESSAGE_EVENT_LOAD_ERROR: {code: "MESSAGE_EVENT_LOAD_ERROR", message:'메세지 이벤트 로드 중 에러가 발생했습니다.'},


} as const;

// 키값들을 타입으로 추출 ("ROOM_NOT_FOUND" | "ALREADY_IN_ROOM" | ...)
export type WsErrorType = keyof typeof WsErrorCode;