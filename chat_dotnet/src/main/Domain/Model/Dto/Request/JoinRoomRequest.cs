namespace Chat.Model.Dto.Request;

public record JoinRoomRequest(
    string RoomId,
    string UserIdx)
{ }