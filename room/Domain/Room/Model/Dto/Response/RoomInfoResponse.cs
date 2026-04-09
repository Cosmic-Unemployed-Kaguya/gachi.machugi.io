namespace Room.Model.Dto.Response;

public record RoomInfoResponse(
    long Idx,
    long HostIdx,
    string name,
    int maxOccupancy,
    long timeLimit,
    bool isPublic,
    long quizIdx
)
{ }