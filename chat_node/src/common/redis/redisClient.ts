import { BaseRes, RedisDto } from '@common/model/base';
import Redis from 'ioredis';
import { Inject, Service } from 'typedi';
import { RoomManager } from '../../room/roomManager';
import config from '../config';


@Service()
export class RedisClient{

    private pubClient: Redis;
    private subClient: Redis;

    constructor(
        @Inject() private roomManager : RoomManager,
    ){
        this.pubClient =  new Redis(config.redisPubServer);
        this.subClient =  new Redis(config.redisSubServer);

        this.subClient.on('message', this.onMessage.bind(this))
    }


    // 특정 채널 구독
    public async onSubscribe(channel : string = 'global_chat_channel') : Promise<void>{
        this.subClient.subscribe(channel);
    }


    // redis에서 메시지 수신 시 
    private async onMessage(channel : string = 'global_chat_channel' , message: string){
        
        const req : RedisDto = JSON.parse(message);

        this.roomManager.sendMessage(req.roomIdx, req.data);

        
    }

    // redis에 메시지 발행
    public async publishMessage(roomIdx : number, data : BaseRes, channel : string ='global_chat_channel' ){
        const payload : RedisDto ={
            roomIdx: roomIdx,
            data: data
        }
        this.pubClient.publish(channel, JSON.stringify(payload));
    }
}