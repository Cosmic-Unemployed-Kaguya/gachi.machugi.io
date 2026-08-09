import { ErrorLevel } from "@cosmic-unemployed-kaguya/grpc-express";
import { ZodError } from "zod";
import { WsError } from "./wsError";


/**
 * @TODO 디테일 수정. 지금은 진짜 대충 구조만 짜놓은 수준 
 * @param err 
 * @returns 
 */
export const wsErrorConverter = (err: any): WsError => {
  let error = err;

  // 내가 정의 한 error가 아닌경우 WsError 형식으로 감싸는 로직
  if (error instanceof WsError) {
    return error;
  }


  // 잘못 된 요청 (validate 에러)
  if (error instanceof ZodError) {
    // 잘못된 요청
    const code: string = 'INVALID_PAYLOAD';

    // 외부 노출용 메시지
    const message: string = "잘못된 요청입니다";

    // 내부 로깅용 메시지
    const internalMessage: string = error.message;

    error = WsError.custom(
      code,
      message,
      true,
      ErrorLevel.WARN,
      err.stack,
      internalMessage,
    );
  }

  // 예상치 못한 에러
  else {
    // error에 statusCode가 있으면 사용, 없으면 일단 500 에러
    //  < TODO : 어떤 오류냐에 따라 더 세세한 구분이 필요함. 해당 부분은 더 찾아보고 추가적인 작업 필요
    const code: string =
      error.code || 'INTERNAL_SERVER_ERROR';

    // 임시!!
    const message: string = '내부 에러 발생';

    // 메시지가 있으면 사용, 없으면 http 상태 코드에 따른 표준 출력
    const internalMessage = error.message;

    error = WsError.custom(
      code,
      message,
      false,
      ErrorLevel.ERROR,
      err.stack,
      internalMessage,
    );
  }

  return error;
};