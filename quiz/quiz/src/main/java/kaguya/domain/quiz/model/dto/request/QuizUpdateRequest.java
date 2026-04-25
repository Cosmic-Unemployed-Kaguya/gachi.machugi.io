package kaguya.domain.quiz.model.dto.request;

public record QuizUpdateRequest (

    String title,
    String description,
    String thumbnail,
    String category
){}
