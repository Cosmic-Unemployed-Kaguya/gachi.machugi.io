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
    public Task<RoomInfoResponse> CreateRoom(CreateRoomRequest request);
    public Task<RoomResponse> FindRoom(long roomIdx);
    public Task<RoomInfoResponse> FindRoomInfo(long roomIdx);
    public Task<RoomSetInfoResponse> FindSetInfo(long roomIdx);
    public Task<RoomInfoResponse> UpdateRoomInfo(long roomIdx, UpdateRoomRequest request);
    public Task<bool> AddPlayerToRoom(long roomIdx, UpdateSetRequest request);
    public Task<bool> RemovePlayerFromRoom(long roomIdx, UpdateSetRequest request);
    public Task<bool> DeleteRoom(long roomIdx);
}