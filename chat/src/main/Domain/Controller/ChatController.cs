using Microsoft.AspNetCore.Mvc;
using Chat.ChatServices;

namespace Chat.ChatControllers;

[ApiController]
[Route("chat")]
public class ChatController : ControllerBase
{
    private readonly ChatService _chatService;

    //의존성 주입
    public ChatController(ChatService chatService)
    {
        _chatService = chatService;
    }

    //외부 서비스가 유저 검증을 끝내고 여기로 HTTP POST 요청을 보낼 거임
    [HttpPost("reserve")]
    public IActionResult ReserveRoute(
        [FromBody] JoinRoomDto dto
    )
    {
        //일단 나중에 예외처리 해야하는데 나중에

        //서비스의 예약맵에 유저 정보와 갈 방을 미리 적어둠, ConnectionId <- 지각
        _chatService.ReserveRoom(dto);

        return Ok();
    }
}