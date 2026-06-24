import { UserInfoListRequset, UserInfoListResponse, UserInfoRequest, UserInfoResponse, UserServiceClient } from "../generated/user";
import config from '../config'
import { GrpcClient, GrpcClientProperty } from "../decorator/grpcClient";


@GrpcClient(UserServiceClient, config.userService)
export default class UserClient {
    
    @GrpcClientProperty()
    public getUserInfo : (userInfoReq : UserInfoRequest) => Promise<UserInfoResponse>;
    
    @GrpcClientProperty()
    public getUserListInfo : (userInfoListReq :UserInfoListRequset) => Promise<UserInfoListResponse>;

}