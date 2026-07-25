import config from "@common/config";
import { GrpcClient, GrpcClientProperty } from "@cosmic-unemployed-kaguya/grpc-express";
import { GetUserNicknameRequest, GetUserNicknameResponse, UserChatGrpcServiceClient } from "@generated/machugi/chat/user";


@GrpcClient(UserChatGrpcServiceClient, config.userService)
export class UserGrpcClient{

    @GrpcClientProperty()
    public getUserNickname: (userIdxReq : GetUserNicknameRequest) => Promise<GetUserNicknameResponse>

}