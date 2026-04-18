
import { Request, Response, NextFunction } from 'express';
import httpStatus from 'http-status'
import { ApiError } from '../utils/error';
import config from '../config';
import logger from '../utils/logger';
import { sendError } from '../model/dto/baseRes';
import { EntityNotFoundError } from 'typeorm';
import { ErrorLevel } from '../model/enum/errorLevel';
import { ZodError } from 'zod';

/** @TODO 에러 처리 핸들러*/
// 1. 어떤 에러가 발생하던 외부로 노출되는 에러 형식은 내가 규정한 형식이어야함. 
// 2. 예상 못한 에러가 나와도 포장하는 작업이 필요
// 3. 해당 핸들러는 express(RestApi) 기준, gRPC의 출력에 맞춘 핸들러는 후에 작성 필요


export const errorConverter = (err : any , req: Request, res: Response, next: NextFunction) =>{
    let error = err;

    // ** switch 문을 안쓰는 이유?
    //  TS에서 switch로 에러를 구분 시, 해당 error의 타입 추론이 안됨..
    // instanceof를 써야 해당 에러가 ApiError 클래스 구나! 를 알 수가 있어 자동완성 등등을 사용 가능함

    // 내가 정의 한 error인 경우
    if ((error instanceof ApiError)){
        
        return next(error)

    }

    // 내가 정의 한 error가 아닌경우 ApiError 형식으로 감싸는 로직

    // ** 데이터 조회 실패(데이터 없음) **
    if(error instanceof EntityNotFoundError){
        
        // 데이터 없음 : NOT FOUND
        const statusCode :number = httpStatus.NOT_FOUND;

        // 외부 노출용 메시지
        const message :string = "존재하지 않는 데이터입니다"

        // 내부 로깅용 메시지
        const internalMessage :string  = error.message ;

        error = new ApiError(statusCode, message , true , ErrorLevel.INFO ,err.stack, internalMessage);
    }

    // 잘못 된 요청 (validate 에러)
    else if(error instanceof ZodError){
                
        // 잘못된 요청
        const statusCode :number = httpStatus.BAD_REQUEST;

        // 외부 노출용 메시지
        const message :string = "잘못된 요청입니다"

        // 내부 로깅용 메시지
        const internalMessage :string  = error.message ;

        error = new ApiError(statusCode, message , true , ErrorLevel.INFO ,err.stack, internalMessage);

    }
    
    // 예상치 못한 에러
    else{

        // error에 statusCode가 있으면 사용, 없으면 일단 500 에러
        //  < TODO : 어떤 오류냐에 따라 더 세세한 구분이 필요함. 해당 부분은 더 찾아보고 추가적인 작업 필요
        const statusCode :number = error.statusCode || httpStatus.INTERNAL_SERVER_ERROR;

        // 외부 노출 메시지는 500에러 기본 문구로?
        const message : string = httpStatus[httpStatus.INTERNAL_SERVER_ERROR];

        // 메시지가 있으면 사용, 없으면 http 상태 코드에 따른 표준 출력
        const internalMessage = error.message || httpStatus[statusCode as keyof typeof httpStatus] ;


        error = new ApiError(statusCode, message, false,  ErrorLevel.ERROR , err.stack, internalMessage);
    }

    next(error);
    

    
}

export const errorHandler = (err : any , req: Request, res: Response, next: NextFunction) =>{

    // 이곳에 들어오는 에러는 위 converter를 거쳐서 오기에 ApiError 형식
    // 에러를 포장 후 출력하는 로직

    // 1. 로그 
    /** @TODO 에러 레벨 별 추가적인 로깅 작업 */
    logger.error(err);

    let {statusCode , message} = err;
    
    // 2. 배포 환경이면서 예상치 못한 에러일경우 ,
    // 위험한 메시지가 노출 안되도록 덮어씌움.
    if(config.profile ==='prod' && !err.isOperational ){
        statusCode = httpStatus.INTERNAL_SERVER_ERROR;
        message = httpStatus[httpStatus.INTERNAL_SERVER_ERROR];
    }

    // 3. res 생성
    const response = {
        code: statusCode,
        message,
        ...(config.profile === 'dev' && { stack: err.stack }),
    } ;


    // 4. res 전송
    sendError(res, statusCode, message);

}