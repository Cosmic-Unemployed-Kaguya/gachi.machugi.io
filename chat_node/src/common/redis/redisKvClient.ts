import config from "@common/config";
import { WsError } from "@common/error/wsError";
import Redis from "ioredis";
import { Service } from "typedi";

@Service()
export class RedisKvClient{

    private kvClient: Redis;

    constructor(){
        this.kvClient =  new Redis(config.redisKvServer);
    }

    public async checkEnterTicket( userIdx : number, roomIdx : number , ticketUuid : string  ): Promise<boolean>{

        const ticketKey = `ticket:${ticketUuid}`;

        // 1. 티켓 조회
        const ticketData = await this.kvClient.get(ticketKey);

        // 1.1 티켓 없으면
        if (!ticketData){
            throw WsError.fromType('INVALID_TICKET');
        }

        // 2. 티켓 확인
        const { userIdx: parsedUserIdx, roomIdx: parsedRoomIdx }= JSON.parse(ticketData);

        // 2.1 티켓의 userIdx와 요청받은 userIdx가 다를경우
        if ( parsedUserIdx != userIdx){
            throw WsError.fromType('UNAUTHORIZED_USER')
        }
        
        // 2.2 티켓의 roomIdx와 요청받은 roomIdx가 다를경우
        if ( parsedRoomIdx != roomIdx){
            throw WsError.fromType('INVALID_ACCESS')
        }

        // 3. 완료시 티켓 제거
        this.kvClient.del(ticketKey);

        return true;
    }
}