using Room.Model.Dto.Request;
using Room.Model.Dto.Response;

namespace Room.Service;
/*
여기서 인터페이스-실체화 하는 이유
1.읽기 편하려고
2.테스트할 때 인터페이스 없으면 귀찮다고 함 
*/
public interface RoomService
{
    public Task<RoomInfoResponse> createRoom(CreateRoomRequest request);
    public Task<RoomResponse> findRoom(long roomIdx);
    public Task<RoomInfoResponse> findRoomInfo(long roomIdx);
    public Task<RoomSetInfoResponse> findSetInfo(long roomIdx);
    public Task<RoomInfoResponse> updateRoomInfo(long roomIdx, UpdateRoomRequest request);
    public Task<bool> addPlayerToRoom(long roomIdx, long playerIdx);
    public Task<bool> removePlayerToRoom(long roomIdx, long playerIdx);
    public Task<bool> deleteRoom(long roomIdx);
}