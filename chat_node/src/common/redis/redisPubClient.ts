import { BaseRes } from '@common/model/base';
import { PublisgKickUserDTO } from '@common/model/kickUser';
import { PublishMessageDto } from '@common/model/publishMessage';
import Redis from 'ioredis';
import { Service } from 'typedi';
import config from '../config';


@Service()
export class RedisPubClient{

    private pubClient: Redis;

    constructor(){
        this.pubClient =  new Redis(config.redisPubServer);
    }


    // redis에 메시지 발행
    public async publishMessage(roomIdx : number, data : BaseRes, channel : string ='global_chat_channel' ){
        const payload : PublishMessageDto ={
            roomIdx: roomIdx,
            data: data
        }
        this.pubClient.publish(channel, JSON.stringify(payload));
    }

    public async publishKickUser(roomIdx : number, userIdx : number ,channel : string ='kick_user_channel'){

        const payload : PublisgKickUserDTO = {
            roomIdx : roomIdx,
            userIdx : userIdx,
        }

        this.pubClient.publish(channel, JSON.stringify(payload))
    }
}