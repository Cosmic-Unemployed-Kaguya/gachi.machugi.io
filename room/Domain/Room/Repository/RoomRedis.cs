using StackExchange.Redis;
using Room.Model.Entity;
using Common;
using Room.Util;
namespace Room.Repository;

public class RoomRedis
{
    //임시용 디폴트 TTL
    private readonly TimeSpan TTL = TimeSpan.FromMinutes(1);
    //이거 레디스 db, 편의성을 위해서 만드는 변수, 성능 거의 하락 없음
    private readonly IDatabase _db;
    //생성자 + 의존성 주입
    public RoomRedis(IConnectionMultiplexer redis)
    {
        _db = redis.GetDatabase();
    }
    private string GetRoomKey(long idx) => $"room:{idx}";
    private string GetPlayerSetKey(long idx) => $"room:{idx}:players";
    //room의 hash를 넣는 걸 '트랜젝션 걸어주는' 함수
    private string RoomHashSetByTransaction(ITransaction tran, ERoom room)
    {
        //key 생성
        string roomKey = GetRoomKey(room.Idx);
        //hash로 넣을 거 정리해서 배열로 생성
        var roomHashArr = room.ToHashArr();
        _ = tran.HashSetAsync(roomKey, roomHashArr);
        return roomKey;
    }
    //room의 PlayerSet 넣는 걸 '트랜젝션 걸어주는' 함수
    private string PlayerSetByTransaction(ITransaction tran, ERoom room)
    {
        //key 생성
        string playerSetKey = GetPlayerSetKey(room.Idx);
        //PlayerSet가 비어있는지 확인
        if (room.PlayerSet.Count > 0)
        {
            //레디스에 넘기기 좋은 배열로 변환
            var playerIdArr = room.PlayerSet.Select(id => (RedisValue)id.ToString()).ToArray();
            //set넣기 예약
            _ = tran.SetAddAsync(playerSetKey, playerIdArr);
        }
        return playerSetKey;
    }
    //TTL 설정 + 시간 연장 메소드
    private void SetTTL(ITransaction tran, string roomKey, string playerSetKey)
    {
        _ = tran.KeyExpireAsync(roomKey, TTL);
        _ = tran.KeyExpireAsync(playerSetKey, TTL);
    }
    //트랜잭션 + 방 upsert 원큐 함수
    private async Task<bool> AllInOneAsync(ERoom room, bool isSetRoom, bool isSetPlayerSet, bool isUpdate = false)
    {
        //트랜젝션 시작
        var tran = _db.CreateTransaction();
        string roomKey = GetRoomKey(room.Idx);
        if (isUpdate)//isUpdate일 때 키가 존재할 때만 수정, 정보 일치
        {
            tran.AddCondition(Condition.KeyExists(roomKey));
        }
        SetTTL(//ttl 설정
            tran,
            //isSetRoom확인해서 true면 방 생성 혹은 수정, 아니면 그냥 방 redis 키만 반환
            isSetRoom ? RoomHashSetByTransaction(tran, room) : roomKey,
            //isSetPlayerSet확인 true면 set 생성 혹은 수정, 아니면 그냥 set redis 키만 반환
            isSetPlayerSet ? PlayerSetByTransaction(tran, room) : GetPlayerSetKey(room.Idx)
        );
        //이제 넣기
        return await tran.ExecuteAsync();
    }
    //저장 메소드
    public async Task<ERoom?> CreateRoomAsync(ERoom room)
    {
        //레디스한테 시켜서 식별자 번호 가져옴, 비동기
        long newIdx = await _db.StringIncrementAsync("room:id:counter");
        //받아온 식별자를 객체에 반영
        room.Idx = newIdx;

        //마법의 저장 함수 + 넣은 거 다시 반환 (확인 차원)
        return await AllInOneAsync(room, true, true) ? await FindRoomByIdxAsync(newIdx) : null;
    }
    //조회 메소드
    public async Task<ERoom?> FindRoomByIdxAsync(long idx)
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
        //가져온 거 합쳐서 서비스에서 사용하는 room 객체 생성
        //마제타라 마제타라 나니 이로 나노카나 room!
        ERoom room = roomHashArr.ToEntity(idx, playerIdArr);
        return room;
    }
    /*
    수정 메소드 room을 받아서 hash만 변경, 
    Action<ERoom>이거 쓰면 람다식 넣을 수 있음
    예를 들어 내가 여기서 Action<ERoom>이렇게 선언해 두면 이걸 쓰는 입장에서는 
    UpdateRoomByIdxAsync(idx, i => ...) 같이 변수를 꺼내면 i의 자료형은 ERoom인거임
    즉 입력값이 ERoom이고 반환값은 void인 간단한 람다식을 받게해주는 거
    */
    public async Task<ERoom?> UpdateRoomByIdxAsync(long idx, Action<ERoom> updateAction)
    {
        //일단 찾아와
        ERoom? room = await FindRoomByIdxAsync(idx);
        if (room == null)
        {
            return null;
        }
        //받은 걸로 수정해, 던져주는 ERoom은 아까 만든 room을 쓴다는 뜻
        updateAction(room);
        //그리고 저장해
        return await AllInOneAsync(room, true, false, isUpdate: true) ? await FindRoomByIdxAsync(room.Idx) : null;
    }
    //삭제 메소드(true 성공, false 실패)
    public async Task<bool> DeleteRoomByIdxAsync(long idx)
    {
        //키 준비
        string roomKey = GetRoomKey(idx);
        string playerSetKey = GetPlayerSetKey(idx);
        //트랜젝션 시작
        var tran = _db.CreateTransaction();
        //key 삭제 예약
        _ = tran.KeyDeleteAsync(roomKey);
        _ = tran.KeyDeleteAsync(playerSetKey);
        return await tran.ExecuteAsync();
    }
    //아직 최대인원 제한 로직은 구현되지 않음
    //방에 플레이어 추가
    public async Task<bool> AddPlayerToRoomAsync(long roomIdx, long playerIdx)
    {
        //방 set의 key
        string playerSetKey = GetPlayerSetKey(roomIdx);
        //트랜젝션 시작 + TTL 새설정
        var tran = _db.CreateTransaction();
        SetTTL(tran, GetRoomKey(roomIdx), playerSetKey);
        //추가
        _ = tran.SetAddAsync(playerSetKey, playerIdx);
        return await tran.ExecuteAsync();
    }
    //방에 플레이어 삭제
    public async Task<bool> RemovePlayerFromRoomAsync(long roomIdx, long playerIdx)
    {
        //방 set의 key
        string playerSetKey = GetPlayerSetKey(roomIdx);
        //트랜젝션 시작 + TTL 새설정
        var tran = _db.CreateTransaction();
        SetTTL(tran, GetRoomKey(roomIdx), playerSetKey);
        //삭제
        _ = tran.SetRemoveAsync(playerSetKey, playerIdx);
        return await tran.ExecuteAsync();
    }
}