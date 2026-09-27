using Game.Model.Dto.Response;
using Room.Service.Proto;

namespace Game.Util;

public static class RoomGrpcMapper
{
    public static RoomInfoResponse ToNativeResponse(this GrpcRoomInfoResponse response)
    {
        return new RoomInfoResponse(
            response.Idx,
            response.HostIdx,
            response.Name,
            response.MaxOccupancy,
            response.TimeLimit,
            response.IsPublic,
            response.QuizIdx
        );
        
    }
}