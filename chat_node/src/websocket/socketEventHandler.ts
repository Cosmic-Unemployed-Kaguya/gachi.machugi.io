import { CustomSocket } from "@common/model/customSocket";
import logger from "@common/util/logger";
import { SocketEvent } from "@decorator/socketEvent";
import { Inject, Service } from "typedi";
import { RoomManager } from "../room/roomManager";


@Service()
export class SocketEventHandler{
    
    constructor(
        @Inject() private roomManager : RoomManager
    ){}

    /**
     * 입력받은 데이터를 내부에서 사용할 수 있게 변환
     * @TODO 유효성 검사
     * @param data 
     */
    // @SocketEvent('message')
    // public message(data : RawData) {
    //     const strData = data.toString('utf-8');
    //     const baseReq: BaseReq = JSON.parse(strData);
    // }

    @SocketEvent('close')
    public close(socket : CustomSocket , code: number, reason: Buffer){
        // @TODO 연결이 끊긴 대상에 대한 후속 조치(방에서 퇴장 등)

        this.roomManager.exitRoom(socket);

        logger.info(`${socket.userNickname} 님과의 연결이 종료되었습니다.`)
        logger.debug(`CLOSE. uid: ${socket.userIdx}, code : ${code}, reason : ${reason}`)
    }

    @SocketEvent('error')
    public error(socket : CustomSocket, error: Error){
        logger.error(error);
        
    }

}