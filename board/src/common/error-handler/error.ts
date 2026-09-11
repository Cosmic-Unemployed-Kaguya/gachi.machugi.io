import { ErrorLevel } from "../model/enum/errorLevel";

/**
 * status : 상태 코드. 기본적으로 HTTP 코드를 따라가지만, gRPC의 경우 번역해서 뱉을거임
 * isOperational : 예상 된 에러인가 ? ex) 서비스 로직 중 내가 뱉은 오류 true, 코드에 문제 생겨서 나는 오류 : false
 * stack : 에러가 발생한 지점
 */
export class ApiError extends Error {
  public status: number;
  public isOperational: boolean;
  public level: ErrorLevel;

  // message : 외부 출력용 메시지

  // 내부 로깅용 메시지
  public internalMessage?: string;

  constructor(
    status: number,
    message: string,
    isOperational: boolean = true,
    level: ErrorLevel = ErrorLevel.WARN,
    stack?: string,
    internalMessage?: string,
  ) {
    super(message);
    this.status = status;
    this.isOperational = isOperational;
    this.level = level;
    this.name = "ApiError";

    if (stack) {
      this.stack = stack;
    } else {
      Error.captureStackTrace(this, this.constructor);
    }

    if (internalMessage) {
      this.internalMessage = internalMessage;
    }
  }
}

export class ForbiddenError extends ApiError {
  constructor(message: string = "권한이 없습니다") {
    super(403, message, true);
    this.name = "ForbiddenError";
  }
}

export class UnauthorizedError extends ApiError {
  constructor(message: string = "로그인이 필요합니다") {
    super(401, message, true);
    this.name = "UnauthorizedError";
  }
}
