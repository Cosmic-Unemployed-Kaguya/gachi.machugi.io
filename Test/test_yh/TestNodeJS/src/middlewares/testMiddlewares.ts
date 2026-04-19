import {Request, Response, NextFunction } from 'express';
import { log } from 'node:console';

export const testMiddle = async (req: Request, res: Response, next: NextFunction) =>{
    // 대충 유효성 검사같은 코드
    log("test111");
    next();

}
export const testMiddle2 =   async (req: Request, res: Response, next: NextFunction) =>{
    // 대충 유효성 검사같은 코드
    const input: string = req.params.name as string;
    if (input == null){
        next('err11')
    }
    log("test2 : " + input);
    next()
}
export const testMiddle3 =   async (req: Request, res: Response, next: NextFunction) =>{
    // 대충 유효성 검사같은 코드
    const {name, description } = req.query;
    if (name == null){
        next('err11')
    }
    if (description == null){
        next('err22')
    }
    log("type orm test : " + name + description);
    next()
}

