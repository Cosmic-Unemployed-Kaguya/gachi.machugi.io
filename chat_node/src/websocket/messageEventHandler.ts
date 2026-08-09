import { WsError } from "@common/error/wsError";
import { BaseReq, BaseRes } from "@common/model/base";
import { CustomSocket } from "@common/model/customSocket";
import { ExitRoomRes } from "@common/model/exitRoom";
import { JoinRoomReq, JoinSuccessRes } from "@common/model/joinRoom";
import { MessageReq, MessageRes } from "@common/model/message";
import logger from "@common/util/logger";
import { MessageEvent } from "@decorator/messageEvent";
import { Inject, Service } from "typedi";
import { RedisPubClient } from "../common/redis/redisPubClient";
import { RoomManager } from "../room/roomManager";

/**
 * 웹프레임워크의 Controller 격.
 * 요청/응답 데이터에 대한 관리.
 */
@Service()
export class MessageEventHandler{

    constructor(
        @Inject() private redisPubClient : RedisPubClient,
        @Inject() private roomManager : RoomManager,
    ){}

    @MessageEvent("join_room")
    public async joinRoom(socket : CustomSocket, baseReq : BaseReq){

        // zod를 통한 유효성 검사
        const req : JoinRoomReq = await JoinRoomReq.parseAsync(baseReq.data)

        const roomIdx : number = req.roomIdx;


        // 입장
        this.roomManager.joinRoom(roomIdx,socket);
        
        // 응답 데이터
        const res : BaseRes = {
            event: "join_room",
            data : {userNickname : socket.userNickname} as JoinSuccessRes
        }
        
        // redis로 전파
        this.redisPubClient.publishMessage(roomIdx ,res);

        logger.info(`방 참여 ${req.roomIdx}`)


    }

    @MessageEvent("chat")
    public async chat(socket : CustomSocket, baseReq : BaseReq){

        if(!socket.roomIdx){
            throw WsError.fromType('NOT_IN_ROOM');
        }

        // 요청 데이터
        const msgReq : MessageReq = await MessageReq.parseAsync(baseReq.data)  

        // 응답 데이터
        const msgRes : MessageRes = {
            msg : msgReq.msg,
            userNickname : socket.userNickname
        }

        const res : BaseRes = {
            event : "chat",
            data : msgRes
        }

        // redis로 전파
        this.redisPubClient.publishMessage(socket.roomIdx, res);

        logger.info(`채팅 : ${socket.userNickname}  : ${msgReq.msg}`)
    }

    @MessageEvent("exit_room")
    public async exitRoom(socket : CustomSocket, baseReq : BaseReq){


        if(!socket.roomIdx) {

            throw WsError.fromType('NOT_IN_ROOM');
        }

        // 보낸 이 방id
        const roomIdx : number = socket.roomIdx;


        // 퇴장
        this.roomManager.exitRoom(socket);

        const res = {
            event: "exit_room",
            data : {userNickname: socket.userNickname} as ExitRoomRes
        }

        this.redisPubClient.publishMessage(roomIdx , res);
        
        logger.info(`퇴장 : ${socket.userNickname}`);
    }


    
}