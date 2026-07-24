import { BaseReq, BaseRes } from "@common/model/base";
import { CustomSocket } from "@common/model/customSocket";
import { ExitRoomRes } from "@common/model/exitRoom";
import { JoinRoomReq, JoinSuccessRes } from "@common/model/joinRoom";
import { MessageReq, MessageRes } from "@common/model/message";
import logger from "@common/util/logger";
import { MessageEvent } from "@decorator/messageEvent";
import { Inject, Service } from "typedi";
import { RedisClient } from "../common/redis/redisClient";
import { RoomManager } from "../room/roomManager";


@Service()
export class MessageEventHandler{

    constructor(
        @Inject() private redisClient : RedisClient,
        @Inject() private roomManager : RoomManager,
    ){}

    @MessageEvent("join_room")
    public joinRoom(socket : CustomSocket, baseReq : BaseReq){

        // @TODO 원래 zod를 쓰던 뭘 쓰던 유효성 검사 필요함
        const req : JoinRoomReq =  baseReq.data
        const roomIdx : number = req.roomIdx;


        // 입장
        this.roomManager.createAndJoinRoom(roomIdx,socket);
        
        // 응답 데이터
        const res : BaseRes = {
            event: "join_room",
            data : {userNickname : socket.userNickname} as JoinSuccessRes
        }
        
        // redis로 전파
        this.redisClient.publishMessage(roomIdx ,res);

        logger.info(`방 참여 ${req.roomIdx}`)


    }

    @MessageEvent("chat")
    public chat(socket : CustomSocket, baseReq : BaseReq){

        if(!socket.roomIdx){
            // TODO 임시
            throw new Error('방에 들어가 있지 않음')
        }

        // 요청 데이터
        const msgReq : MessageReq = baseReq.data  

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
        this.redisClient.publishMessage(socket.roomIdx, res);

        logger.info(`채팅 : ${socket.userNickname}  : ${msgReq.msg}`)
    }

    @MessageEvent("exit_room")
    public exitRoom(socket : CustomSocket, baseReq : BaseReq){


        if(!socket.roomIdx) {
            // TODO 임시
            throw new Error('방에 들어가 있지 않음');
        }

        // 보낸 이 방id
        const roomId : number = socket.roomIdx;


        // 퇴장
        this.roomManager.exitRoom(socket);

        const res = {
            event: "exit_room",
            data : {userNickname: socket.userNickname} as ExitRoomRes
        }

        this.redisClient.publishMessage(roomId, res );
        
        logger.info(`퇴장 : ${socket.userNickname}`);
    }


    
}