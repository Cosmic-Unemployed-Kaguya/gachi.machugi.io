using Room.Model.Dto.Request;

namespace Room.Model.Entity;

public class ERoom
{
    public long Idx { get; set; }// 식별자
    public long HostIdx { get; set; }
    public string Name { get; set; } = string.Empty;//이름
    public int MaxOccupancy { get; set; } = 1;//인원수
    public long TimeLimit { get; set; } = 600;//시간
    public bool IsPublic { get; set; } = true;//공개여부
    public string? Password { get; set; }//방 비밀번호
    public long QuizIdx { get; set; }//방에서 사용하는 퀴즈의 idx값
    public HashSet<long> PlayerSet { get; set; } = new(); //플레이어 set

    public ERoom(
        long idx,
        long hostIdx,
        string name,
        int maxOccupancy,
        long timeLimit,
        bool isPublic,
        string? password,
        long quizIdx
    )
    {
        Idx = idx;
        HostIdx = hostIdx;
        Name = name;
        MaxOccupancy = maxOccupancy;
        TimeLimit = timeLimit;
        IsPublic = isPublic;
        Password = password;
        QuizIdx = quizIdx;
        PlayerSet.Add(hostIdx);
    }
    public void UpdateInfo(UpdateRoomRequest request)
    {
        HostIdx = request.hostIdx ?? HostIdx;
        Name = string.IsNullOrWhiteSpace(request.name) ? Name : request.name;
        MaxOccupancy = request.maxOccupancy ?? MaxOccupancy;
        TimeLimit = request.timeLimit ?? TimeLimit;
        IsPublic = request.isPublic ?? IsPublic;
        Password = string.IsNullOrWhiteSpace(request.password) ? Password : request.name;
        QuizIdx = request.quizIdx ?? QuizIdx;
    }
}