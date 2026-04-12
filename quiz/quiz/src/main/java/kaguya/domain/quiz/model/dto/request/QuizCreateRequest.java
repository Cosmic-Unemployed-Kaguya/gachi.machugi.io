package kaguya.domain.quiz.model.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QuizCreateRequest {

    private String title;
    private String description;
    private String thumbnail;
    private String category;
    private Long creatorId;
}
