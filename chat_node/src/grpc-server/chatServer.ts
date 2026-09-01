import { GrpcServer } from "@cosmic-unemployed-kaguya/grpc-express";
import { ChatGrpcServiceService } from "@generated/machugi/chat/chat";
import { Inject } from "typedi";
import { RedisPubClient } from "../redis/redisPubClient";


@GrpcServer(ChatGrpcServiceService)
export default class ChatGrpcServer {
    constructor(
        @Inject() private redisPubClient : RedisPubClient,
    ){}

    // public async kickUser( req: KickUserRequest ) : Promise<Empty> {
        
    //     await this.redisPubClient.publishKickUser(req.roomIdx, req.userIdx);

    //     // Empty 반환을 위함(void)
    //     return {};
    // }
    
    // public async gameStart (req : GameStartRequest):  Promise<Empty> {
        
    //     return {};
    // }

}