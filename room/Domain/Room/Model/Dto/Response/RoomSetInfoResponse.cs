namespace Room.Model.Dto.Response;

public record RoomSetInfoResponse
(
    HashSet<long> PlayerSet,
    int CurrentCount
)
{ }