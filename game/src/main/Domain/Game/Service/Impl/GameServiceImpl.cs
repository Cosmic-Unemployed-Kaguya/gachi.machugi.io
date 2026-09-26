
using Common;
using Game.Model.Dto.Response;
using Game.Util;
using Room.Service.Proto;

namespace Game.Service;

public class GameServiceImpl : GameService
{
    private readonly RoomGrpcManager.RoomGrpcManagerClient _roomGrpcClient;
    private readonly GameManager _gameManager;
    private readonly RedisSub _redisSub;

    public GameServiceImpl(GameManager gameManager, RedisSub redisSub, RoomGrpcManager.RoomGrpcManagerClient roomGrpcClient)
    {
        _gameManager = gameManager;
        _redisSub = redisSub;
        _roomGrpcClient = roomGrpcClient;
    }   

    // 게임 시작
    public async Task<GameStartResponse> GameStart(long roomIdx)
    {
        var roomInfoRequest = new GrpcRoomIdxRequest
        {
            RoomIdx = roomIdx
        };

        var response = await _roomGrpcClient.FindRoomInfoAsync(roomInfoRequest);
        var room = response.ToNativeResponse();

        if (room == null)
        {
            throw new Exception("room is not found");
        }

        // 1. @TODO 님 방장 맞음?
        // 이거는 유저idx 받아와야함 당장은 스킵

        // 2. GameStart 이벤트 배포 및 세팅
        await _gameManager.InitGame(roomIdx, room.quizIdx, room.timeLimit);

        // 3. 정답 처리 용 redis 구독 시작
        await _redisSub.OnSubscribeCorrect(roomIdx);

        return new GameStartResponse(true);
    }




}