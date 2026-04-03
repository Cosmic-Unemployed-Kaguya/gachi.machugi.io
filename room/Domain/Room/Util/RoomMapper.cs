using Room.Model.Entity;
using Room.Model.Dto.Request;

namespace Room.Util;

public static class RoomMapper
{
    public static ERoom ToEntity(this CreateRoomRequest request)
    {
        ERoom room = new(
            0, //초기화 시점에는 없는 값 idx
            request.name, //이름
            request.maxOccupancy, //최대인원
            request.time, //제한 시간
            request.isPublic, //공개여부
            request.password, //비밀번호
            request.quizIdx //쓰는 퀴즈 식별자
        );
        room.PlayerSet.Add(request.hostId);
        return room;
    }
}