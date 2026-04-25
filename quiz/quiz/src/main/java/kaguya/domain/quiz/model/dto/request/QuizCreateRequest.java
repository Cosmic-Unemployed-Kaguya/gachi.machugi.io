package kaguya.domain.quiz.model.dto.request;

public record QuizCreateRequest(
    String title,
    String description,
    String thumbnail,
    String category,
    Long creatorId
) {
}
