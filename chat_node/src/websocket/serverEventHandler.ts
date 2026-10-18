import { WsError } from "@common/error/wsError";
import { UserGrpcClient } from "@common/grpc-client/userClient";
import { CustomSocket } from "@common/model/customSocket";
import logger from "@common/util/logger";
import { ServerEvent } from "@decorator/serverEvent";
import { GetUserNicknameResponse } from "@generated/machugi/chat/user";
import { IncomingMessage } from "node:http";
import { Inject, Service } from "typedi";
/**
 * connection,  error, headers, close, listening, wsClientError
 */
@Service()
export class ServerEventHandler{
    
    constructor(
        @Inject() private userClient : UserGrpcClient
    ){
        
    }

    @ServerEvent('connection')
    public async connection(socket : CustomSocket, request : IncomingMessage){
        
        // 1. 헤더 까기 
        const headers = request.headers;

        const userIdxStr = headers['x-user-idx']; 
        const userRoleStr = headers['x-user-role'];

        // 1.1 유저 정보 socket에 저장
        const userIdx: number = Number(userIdxStr) 

        // // 테스트용
        // const randomNum : number = crypto.randomInt(0, 100);
        // const userIdx : number  = randomNum;

        if (Number.isNaN(userIdx)) {
            throw WsError.fromType('UNAUTHORIZED_USER')
        }

        socket.userIdx = userIdx;
        
        try {
            const userData : GetUserNicknameResponse = await this.userClient.getUserNickname({userIdx})
            socket.userNickname = userData.nickname;
        } catch{
            // 만약 유저서비스와 통신에 실패 할 경우, 랜덤한 닉네임 부여
            const randomStr = Math.random().toString(36).substring(2, 8);
            socket.userNickname = `user_${randomStr}`;

            // 또는 에러 발생 후 예외처리
            // throw WsError.fromType('INTERNAL_CONNECTION_ERROR')
        }
    }

    @ServerEvent('close')
    public close(){
        // 완전히 서버가 꺼지는 경우.
        // ex) ws.close를 실행하는 등
    }

    @ServerEvent('error')
    public error( error: Error){
        // 이미 서버거 꺼지는 수준의, 꺼저야하는 에러이기에 로그만 남기고 묵념
        logger.error(error)
    }
}