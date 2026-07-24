import { UserGrpcClient } from "@common/grpc-client/userClient";
import { CustomSocket } from "@common/model/customSocket";
import { ServerEvent } from "@decorator/serverEvent";
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

        socket.userIdx = userIdx;
        socket.userNickname = await this.userClient.getUserNickname(userIdx)

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