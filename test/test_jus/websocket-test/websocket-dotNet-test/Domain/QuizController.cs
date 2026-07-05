using Microsoft.AspNetCore.Mvc;

namespace ChatService;

[ApiController]
[Route("/quiz")]
public class QuizController : ControllerBase
{
    private readonly QuizService _quizService;
    public QuizController(QuizService quizService)
    {
        _quizService = quizService;
    }

    // 서버에서 단일 정답을 받는 API
    // Postman으로 POST "/quiz/answer"로 api 쏘면 테스트 할 수 있음
    // 퀴즈 적용은 되는데, 정답 마추는 로직 X (테스트하고 남은 잔재)
    [HttpPost("answer")]
    public async Task<IActionResult> SetQuiz([FromBody] QuizRequest request)
    {
        // 방번호/정답 내용 비어있으면 잘못된 요청
        if (string.IsNullOrEmpty(request.RoomId) || string.IsNullOrEmpty(request.AnswerMessage))
        {
            return BadRequest(new { success = false, message = "잘못된 요청" });
        }

        await _quizService.SetQuiz(request);

        return Ok(new { success = true, message = $"[{request.RoomId}] 퀴즈 시작" });
    }

    // 서버에서 정답 리스트를 받는 API
    [HttpPost("list")]
    public async Task<IActionResult> setQuizList([FromBody] QuizListRequest request)
    {
        if (string.IsNullOrWhiteSpace(request.RoomId) || request.AnswerList == null || !request.AnswerList.Any())
        {
            return BadRequest(new { success = false, message = "잘못된 요청" });
        }

        await _quizService.SetQuizList(request);

        return Ok(new { success = true, message = $"[{request.RoomId}] 방 퀴즈 시작 (리스트)"});
    }
}

// Quiz 서버에서 정답을 받을 DTO 구조 (원래 따로 객체 빼야되는데 귀찮아서...)
public record QuizRequest(string RoomId, string AnswerMessage);
public record QuizListRequest(string RoomId, List<string> AnswerList);