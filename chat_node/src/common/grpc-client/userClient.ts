import config from "@common/config";
import { GrpcClient, GrpcClientProperty } from "@cosmic-unemployed-kaguya/grpc-express";
import { GetUserNicknameRequest, GetUserNicknameResponse, UserInternalServiceClient } from "@generated/machugi/user/user_internal";


@GrpcClient(UserInternalServiceClient, config.userService)
export class UserGrpcClient{

    @GrpcClientProperty()
    public getUserNickname: (userIdxReq : GetUserNicknameRequest) => Promise<GetUserNicknameResponse>

}