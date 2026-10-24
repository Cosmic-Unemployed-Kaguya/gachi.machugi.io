using Game.Model.Vo;

namespace Game.Model.Entity;

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
