import { NextFunction, Response } from "express";
import { ZodError, ZodObject } from "zod";

import { AppRequest } from "./appRequest";

// 아래 파라미터에 들어 올 수 있는 데이터
// ZodObject = DTO
interface ValidationSchemas {
  body?: ZodObject;
  query?: ZodObject;
  params?: ZodObject;
}

/**
 * Route 단계에서 요청 데이터에 대한 명시를 하고 싶었다.
 * - 기존에는 Route에서 요청데이터가 body인지, query인지, 뭐 어떤 데이터들이 들어오는지 알 방법이 없었음
 *
 * > 입력값에 대한 검증은 해야하니 validate 라는 middleware를 만들어
 * > '이런 DTO 형식을 가진 데이터가 body,query,param 중 하나의 방법으로 들어올 것이다 !!'
 * > 라는것을 정의하고 해당 데이터에 문제가 없는지 까지 검사
 *
 * @param schemas
 * @returns
 */
export const validate = (schemas: ValidationSchemas) => {
  return async (req: AppRequest, res: Response, next: NextFunction) => {
    try {
      // 스키마 존재 시 유효성 검사 후 데이터 추가
      if (schemas.body) req.bodyData = await schemas.body.parseAsync(req.body);
      if (schemas.query)
        req.queryData = await schemas.query.parseAsync(req.query);
      if (schemas.params)
        req.paramsData = await schemas.params.parseAsync(req.params);

      next();
    } catch (error) {
      next(error);
    }
  };
};
