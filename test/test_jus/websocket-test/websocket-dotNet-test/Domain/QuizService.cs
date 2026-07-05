using Microsoft.AspNetCore.SignalR;

namespace ChatService;

public class QuizService {

    // SignalR의 QuizHub
    private readonly IHubContext<QuizHub> _hubContext;
    // redis
    private readonly QuizRedis _quizRedis;

    // 생성자
    public QuizService(IHubContext<QuizHub> hubContext, QuizRedis quizRedis)
    {
        _hubContext = hubContext;
        _quizRedis = quizRedis;
    }

    // 단일 퀴즈 세팅 (퀴즈 적용은 되는데, 정답 마추는 로직 X) 
    // 퀴즈 여러개 맞추기 하기전에 이걸 해봤구나.. 정도만
    public async Task SetQuiz(QuizRequest request)
    {
        // redis에 퀴즈 세팅
        await _quizRedis.SetQuizAnswer(request.RoomId, request.AnswerMessage);
        await _hubContext.Clients.Group(request.RoomId)
                .SendAsync("BroadcastMessage", "System(Quiz)", "퀴즈 게임이 시작되었습니다.");
    }

    // 퀴즈 N개 세팅
    public async Task SetQuizList(QuizListRequest request)
    {
        // redis에 퀴즈 리스트 세팅
        await _quizRedis.SetQuizAnswerList(request.RoomId, request.AnswerList);
        await _hubContext.Clients.Group(request.RoomId)
                .SendAsync("BroadcastMessage", "System(Quiz)", "퀴즈 게임이 시작되었습니다. (리스트)");
    }
    
    // 정답 확인
    public async Task<bool> CheckAnswer(string roomId, string userId, string message)
    {
        // 현재 정답 redis에서 가져오기
        var currentAnswer = await _quizRedis.GetCurrentAnswer(roomId);

        // 채팅이 정답일 때
        if (!string.IsNullOrEmpty(currentAnswer) &&
            message.Trim().Equals(currentAnswer, StringComparison.OrdinalIgnoreCase))  // 정답 공백 제거 & 정답이 같은지 비교
        {
            // 정답 처리 (점수 10점 추가)
            await _quizRedis.AddScore(roomId, userId, 10);
            // 현재 스코어 조회
            Dictionary<string, int> currentScore = await _quizRedis.GetAllScores(roomId);
            string scoreboard = string.Join(" / ", currentScore
                    .OrderByDescending(x => x.Value) 
                    .Select(x => $"{x.Key}: {x.Value}점"));
                
            // 정답자 전체 알림
            await _hubContext.Clients.Group(roomId)
                .SendAsync("BroadcastMessage", "System(Quiz)", $"{userId}님이 정답 [{currentAnswer}]을 맞추셨습니다! (+10점)");
            // 현제 스코어 표시
            await _hubContext.Clients.Group(roomId)
                .SendAsync("BroadcastMessage", "System(Quiz)", $"현재 점수 상황 {scoreboard}");

            // 다음 문제 가져오기
            long remainingQuestions = await _quizRedis.NextQuiz(roomId);

            if (remainingQuestions > 0) {
                // 퀴즈가 남아 있다면
                _ = Task.Run(async () =>
                {
                    // 다음 퀴즈까지 잠깐 대기
                    await Task.Delay(3000);
                    await _hubContext.Clients.Group(roomId)
                        .SendAsync("BroadcastMessage", "System(Quiz)", "다음 문제가 시작되었습니다. 정답을 입력하세요!");
                });
            }
            else {
                // 퀴즈가 전부 풀었을 때
                await _hubContext.Clients.Group(roomId)
                    .SendAsync("BroadcastMessage", "System(Quiz)", "모든 퀴즈가 종료되었습니다!");
            }

            return true;
        }

        return false;
    }
}