using StackExchange.Redis;
using Room.Model.Entity;
using Common;
namespace Room.Repository;

public class RoomRedis
{
    //이거 레디스 db, 편의성을 위해서 만드는 변수, 성능 거의 하락 없음
    private readonly IDatabase _db;
    private string GetRoomKey(long idx) => $"room:{idx}";
    private string GetPlayerSetKey(long idx) => $"room:{idx}:players";


    public RoomRedis(IConnectionMultiplexer redis)
    {
        _db = redis.GetDatabase();
    }
    //저장 메소드
    public async Task<ERoom?> SaveAsync(ERoom room)
    {
        //레디스한테 시켜서 식별자 번호 가져옴, 비동기
        long newIdx = await _db.StringIncrementAsync("room:id:counter");
        //받아온 식별자를 객체에 반영
        room.Idx = newIdx;

        //트랜젝션 시작
        var tran = _db.CreateTransaction();
        //key 설정, hash랑 set이랑 분할 해서 저장
        string roomKey = GetRoomKey(room.Idx);
        string playerSetKey = GetPlayerSetKey(room.Idx);
        //hash로 넣을 거 정리해서 배열로 생성
        var roomHashArr = new HashEntry[]
        {
            new("name", room.Name),
            new("max_occupancy", room.MaxOccupancy),
            new("time", room.Time),
            new("is_public", room.IsPublic.ToString().ToLower()),
            new("password", room.Password ?? ""),
            new("quiz_idx", room.QuizIdx),
        };
        //트랜젝션에 hash넣기 예약
        _ = tran.HashSetAsync(roomKey, roomHashArr);
        //PlayerSet가 비어있는지 확인
        if (room.CurrentCount > 0)
        {
            //레디스에 넘기기 좋은 배열로 변환
            var playerIdArr = room.PlayerSet.Select(id => (RedisValue)id.ToString()).ToArray();
            //set넣기 예약
            _ = tran.SetAddAsync(playerSetKey, playerIdArr);
        }
        //이제 넣기
        if (!await tran.ExecuteAsync())
        {
            return null;
        }
        //넣은 거 다시 반환 (확인 차원)
        return await FindBySqAsync(newIdx);
    }
    public async Task<ERoom?> FindBySqAsync(long idx)
    {
        //key 확인
        string roomKey = GetRoomKey(idx);
        string playerSetKey = GetPlayerSetKey(idx);
        //일단 방 정보 가져오기
        var roomHashArr = await _db.HashGetAllAsync(roomKey);
        if (roomHashArr.Length == 0)
        {
            return null;
        }
        //set 정보 가져오기
        var playerIdArr = await _db.SetMembersAsync(playerSetKey);
        //마제타라 마제타라 나니 이로 나노카나
        ERoom room = new(
            idx,
            roomHashArr.GetValue("name", ""),
            int.Parse(roomHashArr.GetValue("max_occupancy", "1")),
            long.Parse(roomHashArr.GetValue("time", "600")),
            bool.Parse(roomHashArr.GetValue("is_public", "true")),
            roomHashArr.GetValue("password", ""),
            long.Parse(roomHashArr.GetValue("quiz_idx", "0"))
        );
        if (playerIdArr != null)
        {
            room.PlayerSet = playerIdArr
            .Where(id => !id.IsNull)
            .Select(id => (long)id).ToHashSet();
        }
        //room!
        return room;
    }
}