import { UserGrpcClient } from "@common/grpc-client/userClient";
import { CustomSocket } from "@common/model/customSocket";
import { ServerEvent } from "@decorator/serverEvent";
import { GetUserNicknameResponse } from "@generated/machugi/chat/user";
import * as crypto from 'crypto';
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
        // const userIdx: number = Number(userIdxStr) 

        // 테스트용
        const randomNum : number = crypto.randomInt(0, 100);
        const userIdx : number  = randomNum;

        if (Number.isNaN(userIdx)) {
            console.error("유효하지 않은 유저 ID입니다:", userIdxStr);
            return;
        }

        socket.userIdx = userIdx;
        
        const userData : GetUserNicknameResponse = await this.userClient.getUserNickname({userIdx})
        socket.userNickname = userData.nickname;
    }

    @ServerEvent('close')
    public close(){
        // 임시.
    }

    @ServerEvent('error')
    public error( error: Error){
        // 에러처리
    }
}