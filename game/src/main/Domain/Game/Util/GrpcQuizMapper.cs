
using GrpcQuiz = Quiz.Service.Proto;
using Game.Model.Dto.Response;
using Game.Model.Vo;
namespace Game.Util;

public static class QuizGrpcMapper
{
    public static GetQuizListResponse  ToNativeResponse(this GrpcQuiz.GrpcGetQuizListResponse response)
    {
        return new  GetQuizListResponse(
            response.Quizzes.Select(item => item.ToNativeItem()).ToList()
        );

    }

    private static QuizData ToNativeItem(this GrpcQuiz.GrpcQuizData quiz)
    {
        return new QuizData
        (
            quiz.SortOrder,
            quiz.ProblemText,
            quiz.ProblemUrl,
            quiz.Type,
            quiz.Answer.ToList()
        );
    }



}