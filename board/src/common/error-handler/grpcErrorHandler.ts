import { Metadata, status } from "@grpc/grpc-js";

import config from "../config";
import logger from "../utils/logger";
import { ApiError, GrpcError } from "./error";
import { errorConverter } from "./errorConvertor";

export const grpcErrorConverter = (err: any): GrpcError => {
  let error = err;

  if (error instanceof GrpcError) {
    return error;
  }
  // ApiError 형식으로 일단 변경
  const convertedApiErr: ApiError = errorConverter(error);

  const grpcStatusCode = getGrpcStatus(convertedApiErr.status);

  // rest 용 응답 코드 > gRPC 응답 코드로 변경
  return new GrpcError(
    grpcStatusCode,
    convertedApiErr.message,
    convertedApiErr.isOperational,
    convertedApiErr.level,
    convertedApiErr.stack,
    convertedApiErr.internalMessage,
  );
};

export const grpcErrorHandler = (err: any): GrpcErrorPayload => {
  // 1. 에러 변환
  let convertedErr: GrpcError = grpcErrorConverter(err);

  // 2. 로그
  logger.error(convertedErr);

  // 3. 반환 데이터
  let errorResponse: GrpcErrorPayload = {
    code: convertedErr.status,
    details: convertedErr.message,
  };

  // 4. 배포 환경이면서 예상치 못한 에러의 경우
  // 위험한 메시지가 노출 안되도록 덮어씌움.
  if (config.profile === "prod" && !convertedErr.isOperational) {
    errorResponse.code = status.INTERNAL;
    errorResponse.details = "처리 중 에러가 발생했습니다";
  }

  return errorResponse;
};

export interface GrpcErrorPayload {
  code: status;
  details: string;
  metadata?: Metadata;
}

export const httpToGrpcStatus: Record<number, status> = {
  400: status.INVALID_ARGUMENT,
  401: status.UNAUTHENTICATED,
  403: status.PERMISSION_DENIED,
  404: status.NOT_FOUND,
  409: status.ALREADY_EXISTS,
  429: status.RESOURCE_EXHAUSTED,
  500: status.INTERNAL,
  501: status.UNIMPLEMENTED,
  503: status.UNAVAILABLE,
  504: status.DEADLINE_EXCEEDED,
};

// 맵핑 안 된 코드는 500으로 처리
export const getGrpcStatus = (httpCode: number): status => {
  return httpToGrpcStatus[httpCode] || status.INTERNAL;
};
