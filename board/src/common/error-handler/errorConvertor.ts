import httpStatus from "http-status";
import { EntityNotFoundError } from "typeorm";
import { ZodError } from "zod";

import { ErrorLevel } from "../model/enum/errorLevel";
import { ApiError } from "./error";

export const errorConverter = (err: any): ApiError => {
  let error = err;

  // 내가 정의 한 error가 아닌경우 ApiError 형식으로 감싸는 로직
  if (error instanceof ApiError) {
    return error;
  }

  // ** 데이터 조회 실패(데이터 없음) **
  if (error instanceof EntityNotFoundError) {
    // 데이터 없음 : NOT FOUND
    const statusCode: number = httpStatus.NOT_FOUND;

    // 외부 노출용 메시지
    const message: string = "존재하지 않는 데이터입니다";

    // 내부 로깅용 메시지
    const internalMessage: string = error.message;

    error = new ApiError(
      statusCode,
      message,
      true,
      ErrorLevel.INFO,
      err.stack,
      internalMessage,
    );
  }

  // 잘못 된 요청 (validate 에러)
  else if (error instanceof ZodError) {
    // 잘못된 요청
    const statusCode: number = httpStatus.BAD_REQUEST;

    // 외부 노출용 메시지
    const message: string = "잘못된 요청입니다";

    // 내부 로깅용 메시지
    const internalMessage: string = error.message;

    error = new ApiError(
      statusCode,
      message,
      true,
      ErrorLevel.INFO,
      err.stack,
      internalMessage,
    );
  }

  // 예상치 못한 에러
  else {
    // error에 statusCode가 있으면 사용, 없으면 일단 500 에러
    //  < TODO : 어떤 오류냐에 따라 더 세세한 구분이 필요함. 해당 부분은 더 찾아보고 추가적인 작업 필요
    const statusCode: number =
      error.statusCode || httpStatus.INTERNAL_SERVER_ERROR;

    // 외부 노출 메시지는 500에러 기본 문구로?
    const message: string = httpStatus[httpStatus.INTERNAL_SERVER_ERROR];

    // 메시지가 있으면 사용, 없으면 http 상태 코드에 따른 표준 출력
    const internalMessage =
      error.message || httpStatus[statusCode as keyof typeof httpStatus];

    error = new ApiError(
      statusCode,
      message,
      false,
      ErrorLevel.ERROR,
      err.stack,
      internalMessage,
    );
  }

  return error;
};
