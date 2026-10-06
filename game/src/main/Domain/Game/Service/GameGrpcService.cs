using Game.Util;
using Grpc.Core;
using GrpcGame = Game.Service.Proto;

namespace Game.Service;


public class GameGrpcService : GrpcGame.GameGrpcManager.GameGrpcManagerBase
{

    private readonly GameService _gameService;

    public GameGrpcService(GameService gameService)
    {
        _gameService = gameService;
    }

    public override async Task<GrpcGame.GrpcGameStartResponse> GameStart(GrpcGame.GrpcGameStartRequest request, ServerCallContext context)
    {
        var nativeResponse = await this._gameService.GameStart(request.RoomIdx);
        return nativeResponse.ToGrpcResponse();
    }
}