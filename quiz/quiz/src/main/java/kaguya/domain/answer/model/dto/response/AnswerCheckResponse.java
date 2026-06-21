package kaguya.domain.answer.model.dto.response;

public record AnswerCheckResponse(
        Long questionIdx,
        boolean correct
) {
}