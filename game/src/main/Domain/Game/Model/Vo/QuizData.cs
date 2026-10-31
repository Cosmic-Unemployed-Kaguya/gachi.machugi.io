
namespace Game.Model.Vo;

public record QuizData
(
    int sortOrder,     // 순서
    string problemText, // 문제 내용
    string problemUrl,  // 문제 미디어 URL
    string type,        // 문제 타입
    List<string> answer       // 정답
)
{ }