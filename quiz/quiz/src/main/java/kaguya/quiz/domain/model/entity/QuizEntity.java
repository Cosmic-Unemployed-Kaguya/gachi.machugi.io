package kaguya.quiz.domain.model.entity;

import java.time.OffsetDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "quiz")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizEntity {
    
    // idx ( PK )
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    // 제목 ( title )
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    // 설명 ( description )
    @Column(name = "description", nullable = false, length = 500)
    private String description;

    // 총 문제 수 ( question_count)
    @Column(name = "question_count", nullable = false)
    private Integer questionCount = 0;

    // 썸네일 ( thumbnail )
    @Column(name = "thumbnail", length = 255)
    private String thumbnail;

    // 제작자 ( creator_id )
    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    // 생성일 ( created_date )
    @Column(name = "created_date", nullable = false)
    private final OffsetDateTime createdDate = OffsetDateTime.now();

    // 수정일 ( updated_date )
    @Column(name = "updated_date", nullable = false)
    private OffsetDateTime updatedDate = OffsetDateTime.now();

    // 카테고리 ( category )
    @Column(name = "category", length = 50)
    private String category;

    @Builder
    public QuizEntity(
            String title,
            String description,
            String thumbnail,
            String category,
            Long creatorId
    ) {
        this.title = title;
        this.description = description;
        this.thumbnail = thumbnail;
        this.category = category;
        this.creatorId = creatorId;
    }

    // 퀴즈 수정 메서드
    public void updateQuiz(
            String title,
            String description,
            String thumbnail,
            String category
    ) {
        this.title = title;
        this.description = description;
        this.thumbnail = thumbnail;
        this.category = category;
        this.updatedDate = OffsetDateTime.now();
    }
}
