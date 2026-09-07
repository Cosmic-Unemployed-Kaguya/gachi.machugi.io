import { WsError } from "@common/error/wsError";
import { BaseReq, BaseRes } from "@common/model/base";
import { CustomSocket } from "@common/model/customSocket";
import { ExitRoomRes } from "@common/model/exitRoom";
import { JoinRoomReq, JoinSuccessRes } from "@common/model/joinRoom";
import { MessageReq, MessageRes } from "@common/model/message";
import logger from "@common/util/logger";
import { MessageEvent } from "@decorator/messageEvent";
import { Inject, Service } from "typedi";
import { RedisKvClient } from "../redis/redisKvClient";
import { RedisPubClient } from "../redis/redisPubClient";
import { RoomManager } from "../room/roomManager";

/**
 * 웹프레임워크의 Controller 격.
 * 요청/응답 데이터에 대한 관리.
 */
@Service()
export class MessageEventHandler{

    constructor(
        @Inject() private redisPubClient : RedisPubClient,
        @Inject() private redisKvClient : RedisKvClient,
        @Inject() private roomManager : RoomManager,
    ){}

    @MessageEvent("join_room", JoinRoomReq)
    public async joinRoom(socket : CustomSocket, req : JoinRoomReq){

        // // 1.zod를 통한 유효성 검사
        // const req : JoinRoomReq = await JoinRoomReq.parseAsync(joinRoomReq)

        const roomIdx : number = req.roomIdx;

        // 2.티켓 확인
        if(!this.redisKvClient.checkEnterTicket(socket.userIdx, req.roomIdx , req.ticketUuid)){
            throw WsError.fromType('INVALID_REQUEST')
        }

        // 3.입장
        this.roomManager.joinRoom(roomIdx,socket);
        
        // 응답 데이터
        const res : BaseRes = {
            event: "join_room",
            data : {userNickname : socket.userNickname} as JoinSuccessRes
        }
        
        // 4.redis로 전파
        this.redisPubClient.publishMessage(roomIdx ,res);

        logger.info(`방 참여 ${req.roomIdx}`)


    }

    @MessageEvent("chat", MessageReq)
    public async chat(socket : CustomSocket, req : MessageReq){

        if(!socket.roomIdx){
            throw WsError.fromType('NOT_IN_ROOM');
        }

        // // 요청 데이터
        // const req : MessageReq = await MessageReq.parseAsync(msgReq)  

        // 응답 데이터
        const msgRes : MessageRes = {
            msg : req.msg,
            userNickname : socket.userNickname
        }
        let res : BaseReq

        // 정답 확인 로직
        const room = this.roomManager.getRoom(socket.roomIdx);
        
        if(room.checkAnswer(req.msg)){
            // 정답일시
            // redis를 통해 선점(SET + NX + EX)
            this.redisKvClient.setWithExpiration(socket.roomIdx, socket.userIdx)

            res = {
                event : "correct",
                data : msgRes
            }

        }else{
            // 그 외
            res = {
                event : "chat",
                data : msgRes
            }
        }

        // redis로 전파
        this.redisPubClient.publishMessage(socket.roomIdx, res);

        logger.info(`채팅 : ${socket.userNickname}  : ${req.msg}`)
    }

    @MessageEvent("exit_room")
    public async exitRoom(socket : CustomSocket){


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