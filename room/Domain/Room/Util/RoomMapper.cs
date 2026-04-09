using Room.Model.Entity;
using Room.Model.Dto.Request;
using Room.Model.Dto.Response;

namespace Room.Util;

public static class RoomMapper
{
    public static ERoom ToEntity(this CreateRoomRequest request)
    {
        return new ERoom(
            0, //초기화 시점에는 없는 값 idx
            request.hostIdx,
            request.name, //이름
            request.maxOccupancy, //최대인원
            request.timeLimit, //제한 시간
            request.isPublic, //공개여부
            request.password, //비밀번호
            request.quizIdx //쓰는 퀴즈 식별자
        );
    }
    public static RoomInfoResponse ToInfoResponse(this ERoom room)
    {
        return new RoomInfoResponse(
            room.Idx,
            room.HostIdx,
            room.Name,
            room.MaxOccupancy,
            room.TimeLimit,
            room.IsPublic,
            room.QuizIdx
        );
    }
    public static RoomSetInfoResponse ToSetInfoResponse(this ERoom room)
    {
        return new RoomSetInfoResponse(
            room.PlayerSet,
            room.CurrentCount
        );
    }
    public static RoomResponse ToRoomResponse(this ERoom room)
    {
        return new RoomResponse(
            room.ToInfoResponse(),
            room.ToSetInfoResponse()
        );
    }
}