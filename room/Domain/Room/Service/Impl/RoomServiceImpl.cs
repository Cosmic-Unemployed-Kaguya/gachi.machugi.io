using Room.Model.Dto.Request;
using Room.Model.Dto.Response;
using Room.Model.Entity;
using Room.Repository;
using Room.Util;

namespace Room.Service;

public class RoomServiceImpl : RoomService
{
    private readonly RoomRedis _roomRedis;
    //생성자
    public RoomServiceImpl(RoomRedis roomRedis)
    {
        _roomRedis = roomRedis;
    }
    //방 만들기
    public async Task<RoomInfoResponse> createRoom(CreateRoomRequest request)
    {
        ERoom? room = await _roomRedis.CreateRoomAsync(request.ToEntity());
        if (room == null) //일단 이런 류의 예외처리는 나중에 공요 예외처리기 만들 듯
        {
            throw new Exception("create room fail");
        }
        return room.ToInfoResponse();
    }
    //방 정보 찾기 함수, RoomResponse = RoomInfoResponse + RoomSetInfoResponse
    public async Task<RoomResponse> findRoom(long roomIdx)
    {
        ERoom? room = await _roomRedis.FindRoomByIdxAsync(roomIdx);
        if (room == null)
        {
            throw new Exception("room is not found");
        }
        return room.ToRoomResponse();
    }
    //이건 방 정보만 찾는 거
    public async Task<RoomInfoResponse> findRoomInfo(long roomIdx)
    {
        ERoom? room = await _roomRedis.FindRoomByIdxAsync(roomIdx);
        if (room == null)
        {
            throw new Exception("room is not found");
        }
        return room.ToInfoResponse();
    }
    //이건 방의 set 정보 = 현재 방의 플레이어들 목록 가져오기
    public async Task<RoomSetInfoResponse> findSetInfo(long roomIdx)
    {
        ERoom? room = await _roomRedis.FindRoomByIdxAsync(roomIdx);
        if (room == null)
        {
            throw new Exception("room is not found");
        }
        return room.ToSetInfoResponse();
    }
    //방 정보 수정, 일단 CreateRoomRequest로 수정하고 있긴 한데 나중에 바꿀 듯
    public async Task<RoomInfoResponse> updateRoomInfo(long roomIdx, CreateRoomRequest request)
    {
        ERoom? room = request.ToEntity();
        room.Idx = roomIdx;
        room = await _roomRedis.UpdateRoomByIdxAsync(room);
        if (room == null)
        {
            throw new Exception("room is not found");
        }
        return room.ToInfoResponse();
    }
    //플레이어 추가
    public Task<bool> addPlayerToRoom(long roomIdx, long playerIdx)
    {
        return _roomRedis.AddPlayerToRoomAsync(roomIdx, playerIdx);
    }
    //플레이어 제거
    public Task<bool> removePlayerToRoom(long roomIdx, long playerIdx)
    {
        return _roomRedis.RemovePlayerFromRoomAsync(roomIdx, playerIdx);
    }
    //방 삭제
    public Task<bool> deleteRoom(long roomIdx)
    {
        return _roomRedis.DeleteRoomByIdxAsync(roomIdx);
    }
}