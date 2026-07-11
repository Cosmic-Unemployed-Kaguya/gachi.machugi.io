using Chat.Model.Dto.Request;
using GrpcChat = Chat.Service.Proto;

namespace Chat.Util;

public static class GrpcChatMapper
{
    public static JoinRoomRequest ToNativeRequest(this GrpcChat.GrpcReserveRoomRequest request)
    {
        return new JoinRoomRequest(
            request.RoomId,
            request.UserIdx
        );
    }
}