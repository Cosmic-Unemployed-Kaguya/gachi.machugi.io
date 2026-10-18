import config from "@common/config";
import { WsError } from "@common/error/wsError";
import Redis from "ioredis";
import { Service } from "typedi";

/**
 * redis key/value 저장/불러오기를 담당하는 클라이언트
 */
@Service()
export class RedisKvClient{

    private kvClient: Redis;

    constructor(){
        this.kvClient =  new Redis(config.redisKvServer);
    }

    /**
     * 방에 유저가 들어오기 전, Room서비스와 상의가 된 이야기인지 확인
     * room에서 발급해준 ticketUuid를 가지고 왔냐?
     */
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


    /**
     * 우선 정답자를 확실히 가리기 위함.
     * SET + NX + EX
     * > 가장 먼저 저장한 놈만 저장되고 그 이후는 저장이 안되는 방식
     */
    public async setWithExpiration(roomIdx: number , userIdx: number){
        const key = "room:" + roomIdx + ":lock";
        const value = userIdx;
        const ttl = 5;

        // NX : 해당 키가 존재하지 않을때에만 저장
        // EX : ttl을 ms가 아닌 sec 단위로 설정 
        const result = await this.kvClient.set(key, value, 'EX', ttl, 'NX');

        if(result === null) throw WsError.fromType('REDIS_ERROR');
    }
}