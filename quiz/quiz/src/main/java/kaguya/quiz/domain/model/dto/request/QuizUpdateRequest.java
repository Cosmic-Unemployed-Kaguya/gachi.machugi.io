package kaguya.quiz.domain.model.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QuizUpdateRequest {

    private String title;
    private String description;
    private String thumbnail;
    private String category;
}
