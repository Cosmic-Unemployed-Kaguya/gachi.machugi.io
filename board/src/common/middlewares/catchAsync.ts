import { NextFunction, Request, Response } from "express";

/**
 * middleware 중 발생하는 오류는 next(err)로 던질 수 있지만
 * controller 아래 서비스 로직에서 발생한 에러는 next(err)로 향하지 않음.
 * >> service 등은 middleware가 아니기에 next함수를 애초에 파라미터로 안받음.
 *
 * 따라서 내부로직 진행 중 err가 발생하여 던져졌을지 받아서 next(err)로 보내는 함수
 *
 * @param fn
 */
export const catchAsync =
  (fn: Function) => (req: Request, res: Response, next: NextFunction) => {
    Promise.resolve(fn(req, res, next)).catch((err) => next(err));
  };
