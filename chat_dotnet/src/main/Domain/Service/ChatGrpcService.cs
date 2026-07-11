using Chat.Util;
using Grpc.Core;
using GrpcChat = Chat.Service.Proto;

namespace Chat.Service;

public class ChatGrpcService : GrpcChat.ChatGrpcManager.ChatGrpcManagerBase
{
    private readonly ChatService _chatService;
    public ChatGrpcService(ChatService chatService)
    {
        _chatService = chatService;
    }
    public override Task<GrpcChat.GrpcReserveRoomResponse> ReserveRoom(GrpcChat.GrpcReserveRoomRequest request, ServerCallContext context)
    {
        var nativeRequest = request.ToNativeRequest();
        //gRPC 응답 포맷으로 리턴
        return Task.FromResult(new GrpcChat.GrpcReserveRoomResponse
        {
            IsSuccess = _chatService.ReserveRoom(nativeRequest),
            Message = "gRPC 기반 방 입장이 정상 예약되었습니다."
        });
    }
}