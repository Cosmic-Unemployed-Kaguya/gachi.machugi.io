package kaguya.domain.quiz.question.model.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "question")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idx")
    private Long idx;

    @Column(name = "quiz_idx", nullable = false)
    private Long quizIdx;

    @Column(name = "problem_text", length = 500)
    private String problemText;

    @Column(name = "problem_url", length = 2048)
    private String problemUrl;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "created_date", nullable = false)
    private final OffsetDateTime createdDate = OffsetDateTime.now();

    @Column(name = "updated_date", nullable = false)
    private OffsetDateTime updatedDate = OffsetDateTime.now();

    @Builder
    public QuestionEntity(
            Long quizIdx,
            String problemText,
            String problemUrl,
            String type,
            Integer sortOrder
    ) {
        this.quizIdx = quizIdx;
        this.problemText = problemText;
        this.problemUrl = problemUrl;
        this.type = type;
        this.sortOrder = sortOrder;
    }

    public void updateQuestion(
            String problemText,
            String problemUrl,
            String type,
            Integer sortOrder
    ) {
        this.problemText = problemText;
        this.problemUrl = problemUrl;
        this.type = type;
        this.sortOrder = sortOrder;
        this.updatedDate = OffsetDateTime.now();
    }
}