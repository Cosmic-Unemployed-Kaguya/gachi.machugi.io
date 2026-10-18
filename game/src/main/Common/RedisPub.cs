

using System.Text.Json;
using Game.Model.Dto.Request;
using Game.Model.Vo;
using StackExchange.Redis;

namespace Common;

public class RedisPub
{
    private readonly ISubscriber _subscriber;
    
    public RedisPub(IConnectionMultiplexer redis)
    {
        _subscriber = redis.GetSubscriber();
    }

    private async Task PublishEvent<T>(long roomIdx, EventRequest<T> req)
    {
        string jsonData = JsonSerializer.Serialize(req);
        string channel = "room" + roomIdx;

        // 정확히 일치하는 채널에
        var redisChannel = RedisChannel.Literal(channel); 
        await _subscriber.PublishAsync(redisChannel, jsonData);
    }

    // 게임 시작 알림
    public async Task GameStartPub(long roomIdx)
    {
        var gameStartEv = new EventRequest<object?>("game_start", null);

        await this.PublishEvent(roomIdx, gameStartEv);
    }

    // 다음 문제 배포
    public async Task NextQuizPub(long roomIdx, QuizData quizData)
    {
        var nextQuizEv = new EventRequest<QuizData>("next_quiz", quizData);
        await this.PublishEvent(roomIdx, nextQuizEv);
    }

    // 퀴즈 타임아웃 알림
    public async Task TimeOutPub(long roomIdx)
    {
        var timeoutEv = new EventRequest<object?>("quiz_timeout", null);
        await this.PublishEvent(roomIdx,timeoutEv);
    }
    
    // 게임 종료 알림
    public async Task GameOverPub(long roomIdx, GameOverRequest request)
    {
        var gameoverEv = new EventRequest<object?>("game_over", request);
        await this.PublishEvent(roomIdx, gameoverEv);
    }

}