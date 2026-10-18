import { BaseRes } from '@common/model/base';
import { CorrectAnswerRes } from '@common/model/correctAnswer';
import Redis from 'ioredis';
import { Service } from 'typedi';
import config from '../common/config';


@Service()
export class RedisPubClient{

    private pubClient: Redis;

    constructor(){
        this.pubClient =  new Redis(config.redisPubServer);
    }


    // redis에 메시지 발행
    public async publishMessage(roomIdx : number, data : BaseRes ){

        this.pubClient.publish(`room:${roomIdx}:base`, JSON.stringify(data));

    }

    public async correctMessage(roomIdx : number, data: CorrectAnswerRes){
        this.pubClient.publish(`room:${roomIdx}:correct`, JSON.stringify(data));
    }
    

    // public async publishKickUser(roomIdx : number, userIdx : number ,channel : string ='kick_user_channel'){

    //     const payload : PublisgKickUserDTO = {
    //         roomIdx : roomIdx,
    //         userIdx : userIdx,
    //     }

    //     this.pubClient.publish(channel, JSON.stringify(payload))
    // }
}