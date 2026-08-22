package kaguya.domain.quiz.model.dto.response;

import java.util.List;

import kaguya.domain.question.model.dto.response.QuestionResponse;

public record QuizStartResponse(
        Long idx,
        String title,
        String description,
        String thumbnailUrl,
        String category,
        List<QuestionResponse> questions
) {
}