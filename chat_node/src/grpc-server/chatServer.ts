import { RedisPubClient } from "@common/redis/redisPubClient";
import { GrpcServer } from "@cosmic-unemployed-kaguya/grpc-express";
import { Empty } from "@generated/google/protobuf/empty";
import { ChatGrpcServiceService, KickUserRequest } from "@generated/machugi/chat/chat";
import { Inject } from "typedi";


@GrpcServer(ChatGrpcServiceService)
export default class ChatGrpcServer {
    constructor(
        @Inject() private redisPubClient : RedisPubClient,
    ){}

    public async kickUser( req: KickUserRequest ) : Promise<Empty> {
        
        await this.redisPubClient.publishKickUser(req.roomIdx, req.userIdx);

        // Empty 반환을 위함(void)
        return {};
    }
    
    // public async gameStart (req : GameStartRequest):  Promise<Empty> {
        
    //     return {};
    // }

}