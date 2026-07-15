using Chat.Service;
using Microsoft.AspNetCore.SignalR;

namespace Chat.ChatControllers;
//Hub가 .net에서 밀어주는 SignalR 라이브러리가 제공하는 거, 상속받으면 컨트롤러로써 기능함
public class ChatHub : Hub
{
    private readonly ChatService _chatService;

    public ChatHub(ChatService chatService)
    {
        _chatService = chatService;
    }
    //유저가 웹소켓으로 서버에 최초 연결되었을 때 자동으로 실행되는 메서드, 등록하고 하트비트함
    //연결되면 hub에서 알아서 실행시킴
    public override async Task OnConnectedAsync()
    {
        var context = Context.GetHttpContext();
        string? userIdx = context?.Request.Query["userIdx"]; //소켓 연결 시점에는 자기가 누구인지만 밝힘
        string connectionId = Context.ConnectionId;

        if (!string.IsNullOrEmpty(userIdx))
        {
            //서비스의 진짜 입장 함수 발동
            bool check = await _chatService.EnterRoom(connectionId, userIdx);

            if (!check)
            {
                //예약맵에 없는 유저라면 소켓 연결 강제 종료
                Context.Abort();
            }
        }

        await base.OnConnectedAsync();
    }
    //유저가 웹소켓 연결을 끊었을 때 자동으로 실행되는 메서드
    //이것도 알아서 실행, 연결 종료 및 리소스 해제, 비정상 종료면 에러도 던져줌
    public override async Task OnDisconnectedAsync(Exception? exception)
    {
        Console.WriteLine($"소켓 연결 해제, ConnectionId : {Context.ConnectionId}");
        //서비스의 연결해제 메서드
        _chatService.Disconnect(Context.ConnectionId);
        //부모에 원래 정의된 OnDisconnectedAsync
        await base.OnDisconnectedAsync(exception);
    }
    //채팅 브로드캐스팅
    public async Task SendMessage(string roomId, string userId, string message)
    {
        //이건 핑퐁
        if (message == "PING")
        {
            await Clients.Caller.SendAsync("BroadcastMessage", "System", "PONG");
            return;
        }
        //방에 있는 모든 사람에게 브로드캐스트
        await Clients.Group(roomId).SendAsync("BroadcastMessage", userId, message);
    }
}