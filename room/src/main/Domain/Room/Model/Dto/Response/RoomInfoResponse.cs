namespace Room.Model.Dto.Response;

public record RoomInfoResponse(
    long Idx,
    long hostIdx,
    string name,
    int maxOccupancy,
    long timeLimit,
    bool isPublic,
    long quizIdx
)
{ }