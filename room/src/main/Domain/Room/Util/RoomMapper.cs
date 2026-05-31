using Room.Model.Entity;
using Room.Model.Dto.Request;
using Room.Model.Dto.Response;
using Common;
using StackExchange.Redis;

namespace Room.Util;

public static class RoomMapper
{
    //CreateRoomRequest -> ERoom
    public static ERoom ToEntity(this CreateRoomRequest request)
    {
        return new ERoom(
            0, //초기화 시점에는 없는 값 idx 대충 아무거나 일단 박음
            request.hostIdx,//방장
            request.name, //이름
            request.maxOccupancy, //최대인원
            request.timeLimit, //제한 시간
            request.isPublic, //공개여부
            request.password, //비밀번호
            request.quizIdx //쓰는 퀴즈 식별자
        );
    }
    //ERoom -> RoomInfoResponse
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
    //ERoom -> RoomSetInfoResponse
    public static RoomSetInfoResponse ToSetInfoResponse(this ERoom room)
    {
        return new RoomSetInfoResponse(
            room.PlayerSet
        );
    }
    //ERoom -> RoomResponse
    public static RoomResponse ToRoomResponse(this ERoom room)
    {
        return new RoomResponse(
            room.ToInfoResponse(),
            room.ToSetInfoResponse()
        );
    }
    //ERoom -> HashEntry[], 레디스에서 저장할 때 씀
    public static HashEntry[] ToHashArr(this ERoom room)
    {
        return [
            new("name", room.Name),
            new("host_idx", room.HostIdx),
            new("max_occupancy", room.MaxOccupancy),
            new("time_limit", room.TimeLimit),
            new("is_public", room.IsPublic.ToString().ToLower()),
            new("password", room.Password ?? ""),
            new("quiz_idx", room.QuizIdx),
        ];
    }
    //HashEntry[](이게 방 정보) + RedisValue[](이건 set) -> ERoom, 레디스에서 조회할 때 씀
    public static ERoom ToEntity(this HashEntry[] hashArr, long idx, RedisValue[]? playerIdArr)
    {
        ERoom room = new ERoom(
            idx,
            long.Parse(hashArr.GetValue("host_idx", "0")),
            hashArr.GetValue("name", ""),
            int.Parse(hashArr.GetValue("max_occupancy", "1")),
            long.Parse(hashArr.GetValue("time_limit", "600")),
            bool.Parse(hashArr.GetValue("is_public", "true")),
            hashArr.GetValue("password", ""),
            long.Parse(hashArr.GetValue("quiz_idx", "0"))
        );
        if (playerIdArr != null)
        {
            room.PlayerSet = playerIdArr
            .Where(id => !id.IsNull)
            .Select(id => (long)id).ToHashSet();
        }
        return room;
    }
}