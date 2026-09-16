using System.Collections.Concurrent;
using Common;
using Game.Model.Dto.Request;
using Game.Model.Vo;
using Game.Util;
using Quiz.Service.Proto;

public class GameRoomState
{
    // quizIdx, TimeLimit, TotalQuizCount는 고민 좀 해봐야할듯
    public long QuizIdx{get;set;}
    public long TimeLimit{get;set;}
    public int TotalQuizCount{get;set;}
    public Queue<QuizData> QuizQueue {get; } = new();
    public int Progress  {get;set;} = 0;
    public CancellationTokenSource? Timer {get;set;} 
}

public class GameManager
{
    // <RoomIdx , 게임상태>
    private ConcurrentDictionary<long, GameRoomState> _rooms = new();
    private readonly RedisPub _redisPub;
    private readonly QuizGrpcManager.QuizGrpcManagerClient _quizGrpcClient;
    public GameManager(RedisPub redisPub, QuizGrpcManager.QuizGrpcManagerClient quizGrpcClient)
    {
        _redisPub = redisPub;
        _quizGrpcClient = quizGrpcClient;
    }

    // 게임 첫 세팅
    public async Task InitGame(long roomIdx, long quizIdx, long timeLimit)
    {
        var roomState = _rooms.GetOrAdd(roomIdx, _ => new GameRoomState() );
        roomState.QuizIdx = quizIdx;
        roomState.TimeLimit = timeLimit;

        // 게임 시작 알림
        await _redisPub.GameStartPub(roomIdx);

        // 일단 모든 문제 가져오는거로..
        var res = await _quizGrpcClient.GetAllQuizAsync(new GrpcGetAllQuizRequest{QuizIdx = (ulong)quizIdx });
        if (res is null || res.Quizzes.Count == 0) throw new Exception("quiz is not found");

        var quizDatas = res.ToNativeResponse();

        foreach(var quiz in quizDatas.quizzes)
        {
            roomState.QuizQueue.Enqueue(quiz);
        }
    }

    // 다음 퀴즈 배포
    // 자동으로 넘어가냐 vs 방장 or 유저가 다음 문제를 클릭하냐
    // 위 선택에 따라 좀 달라질듯. 지금은 자동 기준으로 작성해놓음
    public async Task PubNextQuiz(long roomIdx)
    {
        if(!_rooms.TryGetValue(roomIdx, out var roomState))
        {
            throw new Exception("room is not found");
        }

        // 진행도가 꽉차면
        if(roomState.Progress == roomState.TotalQuizCount)
        {
            // 게임 종료(@TODO 뭐 넣어야 될지 모르겠어서 일단 스킵, 대충 게임 끝날때 유저한테 알려줘야하는거)
            await _redisPub.GameOverPub(roomIdx, new GameOverRequest{});
            return;
        }

        if(!roomState.QuizQueue.TryDequeue(out var quiz))
        {
            throw new Exception("quiz is not found");

        }
        await _redisPub.NextQuizPub(roomIdx , quiz);

        // 진행도 1 추가
        roomState.Progress += 1;
    }

    // 퀴즈 타이머
    public async Task StartQuizTimer(long roomIdx)
    {
        if(!_rooms.TryGetValue(roomIdx, out var roomState))
        {
            throw new Exception("room is not found");
        }
        // 취소 토큰 생성 및 저장
        var cts = new CancellationTokenSource();
        roomState.Timer = cts;

        try
        {
            // 타이머 시작
            await Task.Delay((int)roomState.TimeLimit, cancellationToken: cts.Token);

            // 타이머 종료 시(아무도 못맞춤 등)
            await _redisPub.TimeOutPub(roomIdx);
            await PubNextQuiz(roomIdx);
        }
        catch (OperationCanceledException) when (cts.IsCancellationRequested)
        {
            // 누군가 정답을 맞추면
            await PubNextQuiz(roomIdx);
        }   
        catch
        {
            throw new Exception("Internal Server Error");
        }
        finally
        {
            cts.Dispose();
        }
    }

    public void CorrectAnswer(long roomIdx)
    {
        // @TODO 정답 처리 로직~!~!~!
        // 한명이라도 맞출시 끝 vs 시간 끝날때까지 기다린다?
        // 우선 전자를 생각하고 짬.

        if(!_rooms.TryGetValue(roomIdx, out var roomState))
        {
            throw new Exception("room is not found");
        }
        if (roomState.Timer is null)
        {
            throw new Exception("Internal Server Error");
        }
        // 타이머 종료
        roomState.Timer.Cancel();

        // @TODO 점수 추가 등?? 등?? 


        
    }


}