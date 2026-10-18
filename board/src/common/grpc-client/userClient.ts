import { GrpcClient, GrpcClientProperty } from "@cosmic-unemployed-kaguya/grpc-express";
import config from "../config";


import {
  UserInfoListRequset,
  UserInfoListResponse,
  UserInfoRequest,
  UserInfoResponse,
  UserInternalServiceClient
} from "@generated/machugi/user/user_internal";

@GrpcClient(UserInternalServiceClient, config.userService)
export default class UserClient {
  @GrpcClientProperty()
  public getUserInfo: (userInfoReq: UserInfoRequest) => Promise<UserInfoResponse>;

  @GrpcClientProperty()
  public getUserListInfo: (userInfoListReq: UserInfoListRequset) => Promise<UserInfoListResponse>;
}
