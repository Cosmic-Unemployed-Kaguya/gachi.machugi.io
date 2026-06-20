using Microsoft.AspNetCore.SignalR;

namespace ChatService;
//Hub가 .net에서 밀어주는 SignalR 라이브러리가 제공하는 거, 상속받으면 컨트롤러로써 기능함
public class ChatHub : Hub
{
    //유저가 특정 방에 입장할 때
    //userId는 현재로썬 아직 사용하지 않음.
    public async Task JoinRoom(string roomId, string userId)
    {
        //SignalR 내장 그룹 기능, 방에 유저 커넥션 추가
        //Context.ConnectionId는 웹소켓에 연결될 때 연결된 유저한테 id 발급 해주는 거
        //Groups.AddToGroupAsync를 하면 roomId가 키인 그룹에 유저를 넣어줌. 알아서
        //방은 알아서 만들어짐. 생명주기에 따라 들어간 유저가 0명이 되면 사실상 없어짐.
        //애초에 방이라는게 물리적으로 존재하는게 아니라 논리적인 개념이라 실질적으론 그냥 어느 방에 누가 있다는 명부임.
        await Groups.AddToGroupAsync(Context.ConnectionId, roomId);

        //방에 있는 다른 사람들에게 입장 알림 추가
        //Clients.Group(roomId) roomId가 키인 그룹의 모든 유저를 지목, 리스트를 반환하는게 아님
        //메시지를 쏠 수있는 파이프라인 즉 창구를 열어주는 것
        //첫번째 인자인 "BroadcastMessage"는 string method인데 정해진 건 없고 이벤트이름임.
        //즉 클라이언트에서 method를(여기선 BroadcastMessage) 알고 대기하고 있어야 함. 자바로 치면 이벤트리스너
        await Clients.Group(roomId).SendAsync("BroadcastMessage", "System", $"{userId} 입장");
    }

    //유저가 채팅을 보냈을 때 호출할 메서드
    public async Task SendMessage(string roomId, string userId, string message)
    {
        //PING-PONG
        if (message == "PING")
        {
            await Clients.Caller.SendAsync("BroadcastMessage", "System", "PONG");
        }
        else
        {
            //메시지 브로드캐스팅
            await Clients.Group(roomId).SendAsync("BroadcastMessage", userId, message);
        }
    }
}