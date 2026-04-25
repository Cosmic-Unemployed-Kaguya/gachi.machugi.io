package kaguya.domain.question.model.dto.request;

public record QuestionRequest(
        String problemText,
        String problemUrl,
        String type,
        Integer sortOrder
) {}