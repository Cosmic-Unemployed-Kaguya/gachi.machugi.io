
using Game.Model.Dto.Response;

namespace Game.Service;

public interface GameService
{
    public Task<GameStartResponse> GameStart(long roomIdx);
}