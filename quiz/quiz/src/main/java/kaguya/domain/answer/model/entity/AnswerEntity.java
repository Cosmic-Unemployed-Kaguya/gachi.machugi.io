package kaguya.domain.answer.model.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "answer")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnswerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idx")
    private Long idx;

    @Column(name = "question_idx", nullable = false)
    private Long questionIdx;

    // 정답
    @Column(name = "answer", nullable = false, length = 255)
    private String answer;

    // 필요 있을지 모르겠지만 일단 추가해뒀음22
    @Column(name = "created_date", nullable = false)
    private final OffsetDateTime createdDate = OffsetDateTime.now();

    @Column(name = "updated_date", nullable = false)
    private OffsetDateTime updatedDate = OffsetDateTime.now();

    @Builder
    public AnswerEntity(Long questionIdx, String answer) {
        this.questionIdx = questionIdx;
        this.answer = answer;
    }

    public void patchAnswer(String answer) {
        if (answer != null) {
            this.answer = answer;
        }

        this.updatedDate = OffsetDateTime.now();
    }
}