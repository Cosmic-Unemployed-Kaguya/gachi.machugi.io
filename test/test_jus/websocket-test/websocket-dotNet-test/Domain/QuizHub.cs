using Microsoft.AspNetCore.SignalR;

namespace ChatService;
public class QuizHub : Hub
{
    private readonly QuizService _quizService;

    public QuizHub(QuizService quizService)
    {
        _quizService = quizService;
    }

    // SignalR 메서드에 자세한 동작에 설명은 승주 코드를 참고하시오
    // chat_IS012_SUB001
    public async Task JoinRoom(string roomId, string userId)
    {
        await Groups.AddToGroupAsync(Context.ConnectionId, roomId);
        await Clients.Group(roomId).SendAsync("BroadcastMessage", "System", $"{userId} 입장");
    }

    // 유저가 채팅을 첬을 때
    public async Task SendMessage(string roomId, string userId, string message)
    {
        if (message == "PING")
        {
            await Clients.Caller.SendAsync("BroadcastMessage", "System", "PONG");
            return ;
        }

        // 친 채팅이 퀴즈 정답인지 확인 
        bool isCorrect = await _quizService.CheckAnswer(roomId, userId, message);

        // 정답이 아니면 채팅창에 그냥 메시지 출력
        if (!isCorrect)
        {
            await Clients.Group(roomId).SendAsync("BroadcastMessage", userId, message);
        }
    }
}