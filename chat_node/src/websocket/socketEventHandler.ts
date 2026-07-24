import { CustomSocket } from "@common/model/customSocket";
import logger from "@common/util/logger";
import { SocketEvent } from "@decorator/socketEvent";
import { Service } from "typedi";


@Service()
export class SocketEventHandler{
    
    constructor(){}

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
        // @TODO 임시
        logger.info('연결 종료')
    }

    @SocketEvent('error')
    public error(socket : CustomSocket, error: Error){
        logger.info('에러')
    }

}