import { NextFunction, Request, Response } from "express";
import httpStatus from "http-status";

import config from "../config";
import logger from "../utils/logger";
import { ApiError } from "./error";
import { errorConverter } from "./errorConvertor";

import { sendError } from "@dto/baseRes";

/** @TODO 에러 처리 핸들러*/
// 1. 어떤 에러가 발생하던 외부로 노출되는 에러 형식은 내가 규정한 형식이어야함.
// 2. 예상 못한 에러가 나와도 포장하는 작업이 필요
// 3. 해당 핸들러는 express(RestApi) 기준, gRPC의 출력에 맞춘 핸들러는 후에 작성 필요

export const restErrorConverter = (err: any, req: Request, res: Response, next: NextFunction) => {
  let error = err;

  // ** switch 문을 안쓰는 이유?
  //  TS에서 switch로 에러를 구분 시, 해당 error의 타입 추론이 안됨..
  // instanceof를 써야 해당 에러가 ApiError 클래스 구나! 를 알 수가 있어 자동완성 등등을 사용 가능함

  // 내가 정의 한 error인 경우
  if (error instanceof ApiError) {
    return next(error);
  }

  error = errorConverter(error);

  next(error);
};

export const errorHandler = (err: any, req: Request, res: Response, next: NextFunction) => {
  // 이곳에 들어오는 에러는 위 converter를 거쳐서 오기에 ApiError 형식
  // 에러를 포장 후 출력하는 로직

  // 1. 로그
  /** @TODO 에러 레벨 별 추가적인 로깅 작업 */
  logger.error(err);

  let { statusCode, message } = err;

  // 2. 배포 환경이면서 예상치 못한 에러일경우 ,
  // 위험한 메시지가 노출 안되도록 덮어씌움.
  if (config.profile === "prod" && !err.isOperational) {
    statusCode = httpStatus.INTERNAL_SERVER_ERROR;
    message = httpStatus[httpStatus.INTERNAL_SERVER_ERROR];
  }

  // 3. res 생성
  const response = {
    code: statusCode,
    message,
    ...(config.profile === "dev" && { stack: err.stack }),
  };

  // 4. res 전송
  sendError(res, statusCode, message);
};
