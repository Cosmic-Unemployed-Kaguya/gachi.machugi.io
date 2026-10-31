using Google.Protobuf.WellKnownTypes;
using Grpc.Core;
using Room.Service.Proto;
using Room.Util;
using GrpcInternalRoom = Room.Service.Proto;

namespace Room.Service;

public class RoomGrpcInternalService : GrpcInternalRoom.RoomInternalService.RoomInternalServiceBase
{
    private readonly RoomService _roomService;
    public RoomGrpcInternalService(RoomService roomService)
    {
        _roomService = roomService;
    }

    public override async Task<Empty> DeleteRoom(InnerRoomIdxRequest request, ServerCallContext context)
    {
        bool isSuccess = await _roomService.DeleteRoom(request.RoomIdx);
        return new Empty();
    }

    public override async Task<InnerRoomInfoResponse> FindRoomInfo(InnerRoomIdxRequest request, ServerCallContext context)
    {
        var nativeResponse = await _roomService.FindRoomInfo(request.RoomIdx);
        return nativeResponse.ToInnerResponse();
    }

    // public override Task<Empty> ExitUser(InnerExitUserRequest request, ServerCallContext context)
    // {
    //     return base.ExitUser(request, context);
    // }


    
}