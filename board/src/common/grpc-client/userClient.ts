import { GrpcClient, GrpcClientProperty } from "@cosmic-unemployed-kaguya/grpc-express";
import config from "../config";


import {
  UserInfoListRequset,
  UserInfoListResponse,
  UserInfoRequest,
  UserInfoResponse,
  UserServiceClient,
} from "@generated/machugi/board/user";

@GrpcClient(UserServiceClient, config.userService)
export default class UserClient {
  @GrpcClientProperty()
  public getUserInfo: (userInfoReq: UserInfoRequest) => Promise<UserInfoResponse>;

  @GrpcClientProperty()
  public getUserListInfo: (userInfoListReq: UserInfoListRequset) => Promise<UserInfoListResponse>;
}
