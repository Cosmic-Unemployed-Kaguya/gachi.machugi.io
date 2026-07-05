using Microsoft.AspNetCore.SignalR;
using System.Collections.Concurrent;
using Chat.ChatControllers;

namespace Chat.ChatServices;

public class ChatService
{
    //Hub 외부에서 Groups나 Clients를 제어할 수 있게 해줌
    private readonly IHubContext<ChatHub> _hubContext;
    //실시간맵, ConnectionId(키),UserIdx(값), ConcurrentDictionary가 알아서 동시성 제어 해준다고 함
    //현재 이 방에 붙어 있는 사람
    private static readonly ConcurrentDictionary<string, string> _connectionMap = new();
    //넣기 전 예약맵, UserIdx(키),RoomId(값)
    private static readonly ConcurrentDictionary<string, string> _userRoomReserveMap = new();
    //생성자, 의존성 주입
    public ChatService(IHubContext<ChatHub> hubContext)
    {
        _hubContext = hubContext;
    }
    //Controller가 DTO를 받아서 호출하는 메서드
    public void ReserveRoom(JoinRoomDto dto)
    {
        //예약맵에 저장
        //예약만 하는 이유는 ConnectionId가 아직 안나와서 ConnectionId는 소켓이 연결되고 나면 바로 나옴
        _userRoomReserveMap[dto.UserIdx] = dto.RoomId;
        Console.WriteLine($"입장 예약 {dto.UserIdx}, 방 {dto.RoomId}");
    }
    //유저 입장 로직
    //Hub에 유저가 소켓을 꼽았을 때, 예약맵를 보고 진짜 방에 꽂아주는 메서드
    //hub가 호출, ConnectionId까지 나오면 부르는 메서드
    public async Task<bool> EnterRoom(string connectionId, string userIdx)
    {
        // 예약맵에서 이 유저의 방 정보가 있는지 확인
        if (_userRoomReserveMap.TryGetValue(userIdx, out string? roomId))
        {
            //실시간맵에 등록
            _connectionMap[connectionId] = userIdx;

            //roomId 그룹에 넣음
            await _hubContext.Groups.AddToGroupAsync(connectionId, roomId);

            //입장 완료했으니 예약맵에서는 삭제
            _userRoomReserveMap.TryRemove(userIdx, out _);

            await _hubContext.Clients.Group(roomId).SendAsync("Notice", $"{userIdx}님이 입장하셨습니다.");
            return true;
        }

        return false; //예약맵에 없는 불법 유저인 경우
    }
    //켜놓고 아무런 활동 없는 유저 킥, 강퇴 등등에 쓸 연결
    public void Disconnect(string connectionId)
    {
        //실시간맵에서 connectionId를 찾아 지우기
        //성공하면 지워진 유저의 idx가 userIdx 변수에 담김, c# 개사기 스킬임
        if (_connectionMap.TryRemove(connectionId, out string? userIdx))
        {
            Console.WriteLine($"HandleDisconnect 완료, ConnectionId: {connectionId} (UserIdx: {userIdx})");
            //만약 유저가 나가서 방에 아무도 없다면 방을 터트리거나, 외부 Room 서비스에 REST API 쓸 거면 이 밑
        }
        else
        {
            //이미 지워졌거나 유령인 경우
            Console.WriteLine($"존재하지 않는 번호입니다 : {connectionId}");
        }
    }
}