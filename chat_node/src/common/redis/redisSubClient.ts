import config from "@common/config";
import { PublishMessageDto } from "@common/model/publishMessage";
import { KickUserRequest } from "@generated/machugi/chat/chat";
import Redis from "ioredis";
import { Inject, Service } from "typedi";
import { RoomManager } from "../../room/roomManager";

@Service()
export class RedisSubClient {

    private subClient: Redis;

    constructor(
        @Inject() private roomManager : RoomManager,
    ){

        this.subClient =  new Redis(config.redisSubServer);
        this.subClient.on('message', this.onMessage.bind(this))
    
    }

    // 특정 채널 구독
    public async onSubscribe(channels : string[]) : Promise<void>{
        this.subClient.subscribe(...channels);
    }

    // redis에서 메시지 수신 시 
    private async onMessage(channel : string , message: string){
        
        switch(channel){
            case('global_chat_channel'):
                const messageDto : PublishMessageDto = JSON.parse(message);
                await this.roomManager.sendMessage(messageDto.roomIdx, messageDto.data);
                break;


            case('kick_user_channel'):
                const kickUserDto : KickUserRequest = JSON.parse(message);
                await this.roomManager.kickUser(kickUserDto.roomIdx, kickUserDto.userIdx);
                break;

        }

    }

}