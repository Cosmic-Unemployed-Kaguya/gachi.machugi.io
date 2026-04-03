namespace Room.Model.Entity;

public class ERoom
{
    public long Idx { get; set; }// 식별자
    public string Name { get; set; } = string.Empty;//이름
    public int MaxOccupancy { get; set; } = 1;//인원수
    public long Time { get; set; } = 600;//시간
    public bool IsPublic { get; set; } = true;//공개여부
    public string? Password { get; set; }//방 비밀번호
    public long QuizIdx { get; set; }//방에서 사용하는 퀴즈의 idx값
    public HashSet<long> PlayerSet { get; set; } = new(); //플레이어 set
    public int CurrentCount => PlayerSet.Count; //현재 플레이어 카운트

    public ERoom(
        long idx,
        string name,
        int maxOccupancy,
        long time,
        bool isPublic,
        string? password,
        long quizIdx
    )
    {
        Idx = idx;
        Name = name;
        MaxOccupancy = maxOccupancy;
        Time = time;
        IsPublic = isPublic;
        Password = password;
        QuizIdx = quizIdx;
    }
}