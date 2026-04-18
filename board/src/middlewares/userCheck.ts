import {Request, Response, NextFunction } from 'express';
import logger from '../utils/logger';
import { AppRequest, UserData } from './appRequest';
import { UserRole } from '../model/enum/userRole';
import { ForbiddenError } from '../utils/error';




// 유저 본인 확인
export const userCheck = async (req: AppRequest, res: Response, next: NextFunction) =>{
    
    /**  @TODO 본인 확인 로직 */
    logger.info("대충 본인 확인")
    next();

}

// 유저 권한 확인
export const userRoleCheck = (allowedRole :UserRole[])  => async (req: AppRequest, res: Response, next: NextFunction) =>{
    
    /**  @TODO 유저 정보 받아오기 */

    // 받아온 user에 대한 정보 
    // !!!!!! 테스트 용 데이터 !!!!!!!
    const userData : UserData ={
        userIdx : 1,
        userRole : UserRole.ADMIN,
    }

    // 권한 체크
    if (userData.userRole && !allowedRole.includes(userData.userRole)){
        return next(new ForbiddenError())
    }

    // req에 userdata 넣어주기, 이후 로직에서 꺼낼 쓸 수 있도록
    req.userData = userData

    next();

}

// header의 useridx 확인
export const getUserIdx = async (req: AppRequest, res: Response, next: NextFunction) =>{

    /** @TODO 유저 idx 가져오는 로직 */
    logger.info("유저 idx 가져오는 로직")
    next();

}