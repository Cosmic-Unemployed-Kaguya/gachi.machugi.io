import {Request, Response, NextFunction } from 'express';
import logger from '../utils/logger';
import { AppRequest, UserData } from './appRequest';
import { ForbiddenError, UnauthorizedError } from '../utils/error';
import Container from 'typedi';
import UserClient from '../grpc-client/userClient';
import { UserRole as GrpcRole, UserInfoResponse } from '../generated/machugi/board/user';
import { UserRole as AppRole } from '../model/enum/userRole';
import { toAppRole, toGrpcRole } from '../utils/mapper/userMapper';
import { catchAsync } from '../utils/catchAsync';


/**
 * 유저 정보 가져오기 및 권환 확인까지
 * @param allowedRoles 
 */
export const getUserAndRoleCheck = (allowedRoles :AppRole[])  => catchAsync( async (req: AppRequest, res: Response, next: NextFunction) =>{


    const userIdx :number =  await getUserIdxFromHeader(req);

    const userData :UserInfoResponse = await getUserInfoToService(userIdx);


    // role이 확인이 안 될 경우(role 데이터가 없으면)
    if(!userData.role){
        return next(new ForbiddenError())
    }

    // proto에서 정의한 Enum  -> 내가 정의한 Enum
    const getUserRole : AppRole =  toAppRole(userData.role);

    // 권한 체크
    if (!allowedRoles.includes(getUserRole)){
        return next(new ForbiddenError())
    }

    // req에 userdata 넣어주기, 이후 로직에서 꺼낼 쓸 수 있도록
    req.userData = {userIdx : userIdx,
                    userNickName : userData.nickName,
                    userRole : getUserRole}

    next();

});

/**
 * 유저 정보 가져오기
 * @param req 
 * @param res 
 * @param next 
 */
export const getUserInfo = catchAsync( async (req: AppRequest, res: Response, next: NextFunction)  =>{

    const userIdx :number = await getUserIdxFromHeader(req);

    const userData :UserInfoResponse = await getUserInfoToService(userIdx);

    req.userData = {userIdx : userIdx,
                userNickName : userData.nickName,
                userRole : toAppRole(userData.role)}

    next();

});

/**
 * userIdx 확인
 * @param req 
 * @param res 
 * @param next 
 */
export const getUserIdx = catchAsync( async (req: AppRequest, res: Response, next: NextFunction)  =>{
    const userIdx :number = await getUserIdxFromHeader(req);
    req.userData = {userIdx : userIdx} ; 
    next();
});


//--------------------------------------------------
// 아래로는 해당 middle ware에서 사용 할 공통 함수
//--------------------------------------------------

const getUserInfoToService = async (userIdx : number) : Promise<UserInfoResponse> =>{
    
    const userClient = Container.get(UserClient);

    return await userClient.getUserInfo({userIdx : userIdx});

}

const getUserIdxFromHeader = async (req: AppRequest) : Promise<number> => {

    /** @TODO 헤더에서 idx 가져오는 로직 */
    const userIdx :number = 1 ;

    if (!userIdx){
        throw new UnauthorizedError();
    }

    return userIdx;
}