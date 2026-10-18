import config from "@common/config";
import { GrpcClient, GrpcClientProperty } from "@cosmic-unemployed-kaguya/grpc-express";
import { Empty } from "@generated/google/protobuf/empty";
import { DeleteRoomRequest, ExitUserRequest, RoomGrpcServiceClient } from "@generated/machugi/room/room";


@GrpcClient(RoomGrpcServiceClient, config.roomService)
export class RoomGrpcClient{

    constructor(){}

    @GrpcClientProperty()
    public exitUser : (req :ExitUserRequest) => Promise<Empty>

    @GrpcClientProperty()
    public deleteRoom : (req : DeleteRoomRequest) => Promise<Empty>
 

}