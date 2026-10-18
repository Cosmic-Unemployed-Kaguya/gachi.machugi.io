import config from "@common/config";
import { GrpcClient, GrpcClientProperty } from "@cosmic-unemployed-kaguya/grpc-express";
import { Empty } from "@generated/google/protobuf/empty";
import { DeleteRoomRequest, ExitUserRequest, RoomInternalServiceClient } from "@generated/machugi/room/room_internal";


@GrpcClient(RoomInternalServiceClient, config.roomService)
export class RoomGrpcClient{

    constructor(){}

    @GrpcClientProperty()
    public exitUser : (req :ExitUserRequest) => Promise<Empty>

    @GrpcClientProperty()
    public deleteRoom : (req : DeleteRoomRequest) => Promise<Empty>
 

}