import config from "@common/config";
import { BaseRes } from "@common/model/base";
import { QuizAnswerReq, QuizData } from "@common/model/quiz";
import logger from "@common/util/logger";
import Redis from "ioredis";
import { Inject, Service } from "typedi";
import { RoomManager } from "../room/roomManager";

@Service()
export class RedisSubClient {

    private subClient: Redis;

    @Inject(() => RoomManager)
    private roomManager: RoomManager;

    constructor(
        // @Inject(() => RoomManager) private roomManager : RoomManager,
    ){

        this.subClient =  new Redis(config.redisSubServer);
        this.subClient.on('message', this.onMessage.bind(this))
    
    }

    // // 특정 채널 구독
    // public async onSubscribe(channels : string[]) : Promise<void>{
    //     this.subClient.subscribe(...channels);
    // }
    public async onSubscribe(channel : string) : Promise<void>{
        await this.subClient.subscribe(channel);
    }

    // 구독 해지
    public async unSubscribe(channel : string) : Promise<void>{
        await this.subClient.unsubscribe(channel);
    }


    // redis에서 메시지 수신 시 
    // @TODO 하 이거 꼴보기 싫은데 언제 리펙토링하냐...
    private async onMessage(channel : string , message: string){
        
        try{
            logger.info(`[Redis 수신] 채널: ${channel}, 원본 메시지: ${message}`);
            // channel = room:{roomIdx}
            const roomIdxStr = channel.split(':')[1];

            const roomIdx : number= Number(roomIdxStr)

            const payload : BaseRes = JSON.parse(message);

            switch(payload.event){

                // 채팅이 들어오면 넘겨주기만 ㅇㅇ
                case('chat'):
                    await this.roomManager.sendMessage(roomIdx, payload);
                    break;

                case('join_room'):
                    await this.roomManager.sendMessage(roomIdx, payload);
                    break;

                case('exit_room'):
                    await this.roomManager.sendMessage(roomIdx, payload);
                    break;

                // room에게 게임 시작 요청을 받을 경우, 유저에게 알림
                case('game_start'):
                    // 필요 시에 client에게서 준비 완료를 받을거임
                    await this.roomManager.sendMessage(roomIdx, payload);
                    break;


                // 누군가가 정답을 맞춘경우. 똑같이 메시지만 날려주면 될듯함
                case('correct'):
                    await this.roomManager.sendMessage(roomIdx, payload);
                    break;

                // room에게 다음 문제 요청받으면, 유저한테 문제 던지면서 저장된 정답 수정
                case('next_quiz'):
                    
                    const quizData : QuizAnswerReq = await QuizAnswerReq.parseAsync(payload.data);

                    const room = this.roomManager.getRoom(roomIdx);
                    
                    const message : BaseRes = {
                        event : 'next_quiz',
                        data :{
                            problemText : quizData.problemText,
                            problemUrl : quizData.problemUrl,
                            sortOrder : quizData.sortOrder,
                            type : quizData.type
                        } as QuizData
                    }

                    // client에 다음 문제 전송
                    await room.sendMessage(message);

                    // room에 저장되어있는 정답 수정
                    room.setAnswer(quizData.answer);

                    break;
                
            }
        }catch(error){
            logger.error('redis Sub 처리 실패 : ' ,error);
        }

        // try{
        //     switch(channel){
        //         case('global_chat_channel'):
            //             const messageDto : PublishMessageDto = JSON.parse(message);
            //             await this.roomManager.sendMessage(messageDto.roomIdx, messageDto.data);
        //             break;


        //         case('game_start'):
        //             const gameStartReq : GameStartReq = JSON.parse(message);
        //             await this.roomManager.sendMessage(gameStartReq.roomIdx, {event : 'game_start'});
        //             break;


        //         case('next_quiz'):
                    
        //             break;
                
        //         case('correct'):
        //             break;

        //         // case('kick_user_channel'):
        //         //     const kickUserDto : KickUserRequest = JSON.parse(message);
        //         //     await this.roomManager.kickUser(kickUserDto.roomIdx, kickUserDto.userIdx);
        //         //     break;

        //     }
        // }catch(error){
        //     logger.error('redis Sub 처리 실패 : ' ,error);
        // }


    }

}