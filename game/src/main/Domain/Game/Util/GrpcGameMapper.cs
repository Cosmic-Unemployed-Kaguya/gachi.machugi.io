using System.Linq;
using Game.Model.Dto.Request;
using Game.Model.Dto.Response;
using GrpcGame = Game.Service.Proto;

namespace Game.Util;

public static class GameGrpcMapper
{
    public static GameStartRequest ToNativeRequest(this GrpcGame.GrpcGameStartRequest request)
    {
        return new GameStartRequest(
            request.RoomIdx
        );
    }


    public static GrpcGame.GrpcGameStartResponse ToGrpcResponse(this GameStartResponse response)
    {
        return new GrpcGame.GrpcGameStartResponse
        {
            IsSuccess = response.success
        };
    }



}