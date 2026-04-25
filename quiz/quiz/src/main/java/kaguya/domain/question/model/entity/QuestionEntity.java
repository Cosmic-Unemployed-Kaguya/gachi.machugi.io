package kaguya.domain.question.model.entity;

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

    // 문제 idx
    @Column(name = "quiz_idx", nullable = false)
    private Long quizIdx;

    // 문제 내용
    @Column(name = "problem_text", length = 500)
    private String problemText;

    // 문제 url
    @Column(name = "problem_url", length = 2048)
    private String problemUrl;

    // 문제 type (ex:image)
    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    // 필요 있으려나 없으려나 일단 추가해둠
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
        if (problemText != null) {
        this.problemText = problemText;
        }

        if (problemUrl != null) {
            this.problemUrl = problemUrl;
        }

        if (type != null) {
            this.type = type;
        }

        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }

        this.updatedDate = OffsetDateTime.now();
        }
}