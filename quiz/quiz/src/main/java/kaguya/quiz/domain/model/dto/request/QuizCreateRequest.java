package kaguya.quiz.domain.model.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QuizCreateRequest {

    // 나중에 NOTBLANK, NOTNULL 등 validation같은 validation 추가할 예정
    private String title;
    private String description;
    private String thumbnail;
    private String category;
    private Long creatorId;
}
