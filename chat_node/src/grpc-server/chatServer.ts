import { GrpcServer } from "@cosmic-unemployed-kaguya/grpc-express";
import { ChatNodeGrpcServiceService } from "@generated/machugi/chat/chat_node";
import { Inject } from "typedi";
import { RedisPubClient } from "../redis/redisPubClient";


@GrpcServer(ChatNodeGrpcServiceService)
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