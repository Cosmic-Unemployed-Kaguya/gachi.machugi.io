using Game.Model.Vo;

namespace Game.Model.Dto.Response;

public record GetQuizListResponse
(
    List<QuizData> quizzes // 퀴즈 리스트
)
{ }
