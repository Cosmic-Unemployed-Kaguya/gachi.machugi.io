

using System.Text.Json;
using Game.Model.Dto.Response;
using StackExchange.Redis;

namespace Common;

public class RedisSub
{
    private readonly ISubscriber _subscriber;
    private readonly GameManager _gameManager;
    
    public RedisSub(ISubscriber subscriber, GameManager gameManager)
    {
        _subscriber = subscriber;
        _gameManager = gameManager;
    }
    
    // 정답 알림을 받기 위한 구독 메서드
    public async Task OnSubscribeCorrect(long roomIdx)
    {   
        
        var redisChannel = RedisChannel.Literal($"room:{roomIdx}:correct"); 

        await _subscriber.SubscribeAsync(redisChannel, (channel, message) =>
        {   
            if (message.IsNullOrEmpty) return;

            try
            {
                CorrectUserResponse? req = JsonSerializer.Deserialize<CorrectUserResponse>(message!);
                if (req is null) return;
            }
            catch
            {
                // @TODO 처리를 어케 해야하나
                //throw new Exception("Bad Request");
                return;
            }

            _gameManager.CorrectAnswer(roomIdx);
        });
    }

    public async Task UnSubscribeCorrect(long roomIdx)
    {
        var redisChannel = RedisChannel.Literal($"room:{roomIdx}:correct"); 
        await _subscriber.UnsubscribeAsync(redisChannel);
    }




}