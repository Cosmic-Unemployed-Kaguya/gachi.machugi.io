using System.Windows.Markup;
using StackExchange.Redis;

namespace ChatService;

// 퀴즈 관련 redis 로직
public class QuizRedis
{
    private readonly IDatabase _redis;
    public QuizRedis(IConnectionMultiplexer redis)
    {
        _redis = redis.GetDatabase();
    }

    // 단일 퀴즈 세팅
    public async Task SetQuizAnswer(string roomId, string answerMessage)
    {
        var key = $"quiz:{roomId}";
        await _redis.KeyDeleteAsync(key);
        await _redis.StringSetAsync(key, answerMessage);
    }

    // 퀴즈 리스트 세팅
    public async Task SetQuizAnswerList(string roomId, List<string> answerList)
    {
        var quizKey = $"quiz:{roomId}:list";
        var scoreKey = $"quiz:{roomId}:scores";
        await _redis.KeyDeleteAsync(quizKey);
        await _redis.KeyDeleteAsync(scoreKey);

        // 앞뒤 공백 자르고, Redis 전용 데이터 타입으로 형변환 -> 이걸 Array로
        var redisValues = answerList.Select(a => (RedisValue)a.Trim()).ToArray();
    
        // redis에 퀴즈 push
        await _redis.ListRightPushAsync(quizKey, redisValues);
    }

    // 현재 정답 조회 (조회만)
    public async Task<string?> GetCurrentAnswer(string roomId)
    {
        // 가장 처음 값 조회하는 명령어 없음
        // 인덱스로 찾기 (0번)
        var value = await _redis.ListGetByIndexAsync($"quiz:{roomId}:list", 0);
        return value.HasValue ? value.ToString() : null;
    }

    // 다음 문제 가져오기
    public async Task<long> NextQuiz(string roomId)
    {
        var key = $"quiz:{roomId}:list";
        
        // 맞춘 퀴즈 제거(pop) 하고 다음 문제 세팅
        await _redis.ListLeftPopAsync(key);
        
        // 퀴즈 몇 개 남았는지 반환
        return await _redis.ListLengthAsync(key);
    }

    public async Task AddScore(string roomId, string userId, int score)
    {
        var key = $"quiz:{roomId}:scores";

        // 원자적 처리
        await _redis.HashIncrementAsync(key, userId, score);
    }

    public async Task<Dictionary<string, int>> GetAllScores(string roomId)
    {
        var key = $"quiz:{roomId}:scores";

        var values = await _redis.HashGetAllAsync(key);

        // redis 값 dictionary로 변경
        return values.ToDictionary(
            entry => entry.Name.ToString(),  // key
            entry => (int)entry.Value  // value
        );
    }
}