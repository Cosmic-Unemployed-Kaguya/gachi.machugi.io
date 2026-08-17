import { CustomSocket } from "@common/model/customSocket";
import { ExitRoomRes } from "@common/model/exitRoom";
import { RedisPubClient } from "@common/redis/redisPubClient";
import logger from "@common/util/logger";
import { SocketEvent } from "@decorator/socketEvent";
import { Inject, Service } from "typedi";
import { RoomManager } from "../room/roomManager";
import { RoomGrpcClient } from './../common/grpc-client/roomClient';


@Service()
export class SocketEventHandler{
    
    constructor(
        @Inject() private roomManager : RoomManager,
        @Inject() private redisPubClient : RedisPubClient,
        @Inject() private roomGrpcClient :RoomGrpcClient
    ){}


    @SocketEvent('close')
    public close(socket : CustomSocket , code: number, reason: Buffer){
        
        const roomIdx = socket.roomIdx;
        
        // 끊어진 유저가 특정 방에 들어가 있었다면?
        if (roomIdx){

            // 1. 퇴장
            this.roomManager.exitRoom(socket);

            // 2. 해당 방 유저들에게 퇴장 알림
            const res = {
                event: "exit_room",
                data : {userNickname: socket.userNickname} as ExitRoomRes
            }

            this.redisPubClient.publishMessage(roomIdx , res);

            // 3. room 서비스에 알림
            this.roomGrpcClient.exitUser({roomIdx : roomIdx, userIdx : socket.userIdx})
        }


        logger.info(`${socket.userNickname} 님과의 연결이 종료되었습니다.`)
        logger.debug(`CLOSE. uid: ${socket.userIdx}, code : ${code}, reason : ${reason}`)
    }

    @SocketEvent('error')
    public error(socket : CustomSocket, error: Error){
        logger.error(error);
        
    }

}