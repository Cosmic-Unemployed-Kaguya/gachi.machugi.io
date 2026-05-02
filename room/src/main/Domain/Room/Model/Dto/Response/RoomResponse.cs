namespace Room.Model.Dto.Response;

public record RoomResponse
(
    RoomInfoResponse roomInfo,
    RoomSetInfoResponse setInfo
)
{ }