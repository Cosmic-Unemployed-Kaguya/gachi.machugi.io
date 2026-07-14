using Grpc.Core;
using Room.Util;
using GrpcRoom = Room.Service.Proto;

namespace Room.Service;

//proto 명세에서 정의한 RoomGrpcManager의 Base 클래스를 상속, 이름 한번 기괴하네
public class RoomGrpcService : GrpcRoom.RoomGrpcManager.RoomGrpcManagerBase
{
    private readonly RoomService _roomService;
    public RoomGrpcService(RoomService roomService)
    {
        _roomService = roomService;
    }

    //방 만들기 창구
    public override async Task<GrpcRoom.GrpcRoomInfoResponse> CreateRoom(GrpcRoom.GrpcCreateRoomRequest request, ServerCallContext context)
    {
        var nativeResponse = await _roomService.CreateRoom(request.ToNativeRequest());
        return nativeResponse.ToGrpcResponse();
    }

    //방 전체 정보 찾기
    public override async Task<GrpcRoom.GrpcRoomResponse> FindRoom(GrpcRoom.GrpcRoomIdxRequest request, ServerCallContext context)
    {
        var nativeResponse = await _roomService.FindRoom(request.RoomIdx);
        return nativeResponse.ToGrpcResponse();
    }

    //방 단일 정보만 찾기
    public override async Task<GrpcRoom.GrpcRoomInfoResponse> FindRoomInfo(GrpcRoom.GrpcRoomIdxRequest request, ServerCallContext context)
    {
        var nativeResponse = await _roomService.FindRoomInfo(request.RoomIdx);
        return nativeResponse.ToGrpcResponse();
    }

    //방에 속한 플레이어 Set 정보만 찾기
    public override async Task<GrpcRoom.GrpcRoomSetInfoResponse> FindSetInfo(GrpcRoom.GrpcRoomIdxRequest request, ServerCallContext context)
    {
        var nativeResponse = await _roomService.FindSetInfo(request.RoomIdx);
        return nativeResponse.ToGrpcResponse();
    }

    //방 정보 수정 창구
    public override async Task<GrpcRoom.GrpcRoomInfoResponse> UpdateRoomInfo(GrpcRoom.GrpcUpdateRoomRequest request, ServerCallContext context)
    {
        //request.RoomIdx와 변환된 바디 DTO를 쪼개서 기존 서비스에 전달
        var nativeResponse = await _roomService.UpdateRoomInfo(request.RoomIdx, request.ToNativeRequest());
        return nativeResponse.ToGrpcResponse();
    }

    //플레이어 추가
    public override async Task<GrpcRoom.GrpcBoolResponse> AddPlayerToRoom(GrpcRoom.GrpcUpdateSetRequest request, ServerCallContext context)
    {
        bool isSuccess = await _roomService.AddPlayerToRoom(request.RoomIdx, request.ToNativeRequest());
        return new GrpcRoom.GrpcBoolResponse { IsSuccess = isSuccess };
    }

    //플레이어 제거
    public override async Task<GrpcRoom.GrpcBoolResponse> RemovePlayerFromRoom(GrpcRoom.GrpcUpdateSetRequest request, ServerCallContext context)
    {
        bool isSuccess = await _roomService.RemovePlayerFromRoom(request.RoomIdx, request.ToNativeRequest());
        return new GrpcRoom.GrpcBoolResponse { IsSuccess = isSuccess };
    }

    //방 삭제 창구
    public override async Task<GrpcRoom.GrpcBoolResponse> DeleteRoom(GrpcRoom.GrpcRoomIdxRequest request, ServerCallContext context)
    {
        bool isSuccess = await _roomService.DeleteRoom(request.RoomIdx);
        return new GrpcRoom.GrpcBoolResponse { IsSuccess = isSuccess };
    }
}