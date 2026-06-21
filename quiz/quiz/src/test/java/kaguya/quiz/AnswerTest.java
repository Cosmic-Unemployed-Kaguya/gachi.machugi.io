package kaguya.quiz;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kaguya.domain.answer.model.dto.request.AnswerCheckRequest;
import kaguya.domain.answer.model.dto.response.AnswerCheckResponse;
import kaguya.domain.answer.model.entity.AnswerEntity;
import kaguya.domain.answer.repository.AnswerRepository;
import kaguya.domain.answer.service.impl.AnswerServiceImpl;
import kaguya.domain.answer.util.mapper.AnswerMapper;

@ExtendWith(MockitoExtension.class)
class AnswerTest {

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private AnswerMapper answerMapper;

    private AnswerServiceImpl answerService;

    @BeforeEach
    void setUp() {
        answerService = new AnswerServiceImpl(
                answerRepository,
                answerMapper
        );
    }

    @Test
    @DisplayName("입력한 답과 저장된 정답이 같으면 정답을 반환한다")
    void checkAnswerSameAnswer() {
        Long questionIdx = 1L;

        AnswerEntity answer = AnswerEntity.builder()
                .questionIdx(questionIdx)
                .answer("Spring Boot")
                .build();

        when(answerRepository.findByQuestionIdx(questionIdx))
                .thenReturn(List.of(answer));

        AnswerCheckResponse response = answerService.checkAnswer(
                questionIdx,
                new AnswerCheckRequest("Spring Boot")
        );

        assertThat(response.questionIdx()).isEqualTo(questionIdx);
        assertThat(response.correct()).isTrue();
    }

    @Test
    @DisplayName("대소문자가 달라도 정답을 반환한다")
    void checkAnswerIgnoreCase() {
        Long questionIdx = 1L;

        AnswerEntity answer = AnswerEntity.builder()
                .questionIdx(questionIdx)
                .answer("Spring Boot")
                .build();

        when(answerRepository.findByQuestionIdx(questionIdx))
                .thenReturn(List.of(answer));

        AnswerCheckResponse response = answerService.checkAnswer(
                questionIdx,
                new AnswerCheckRequest("SPRING BOOT")
        );

        assertThat(response.correct()).isTrue();
    }

    @Test
    @DisplayName("앞뒤 공백이 있어도 정답을 반환한다")
    void checkAnswerIgnoreOuterWhitespace() {
        Long questionIdx = 1L;

        AnswerEntity answer = AnswerEntity.builder()
                .questionIdx(questionIdx)
                .answer("Spring Boot")
                .build();

        when(answerRepository.findByQuestionIdx(questionIdx))
                .thenReturn(List.of(answer));

        AnswerCheckResponse response = answerService.checkAnswer(
                questionIdx,
                new AnswerCheckRequest("   Spring Boot   ")
        );

        assertThat(response.correct()).isTrue();
    }

    @Test
    @DisplayName("내부 띄어쓰기가 달라도 정답을 반환한다")
    void checkAnswerIgnoreInnerWhitespace() {
        Long questionIdx = 1L;

        AnswerEntity answer = AnswerEntity.builder()
                .questionIdx(questionIdx)
                .answer("Spring Boot")
                .build();

        when(answerRepository.findByQuestionIdx(questionIdx))
                .thenReturn(List.of(answer));

        AnswerCheckResponse response = answerService.checkAnswer(
                questionIdx,
                new AnswerCheckRequest("SpringBoot")
        );

        assertThat(response.correct()).isTrue();
    }

    @Test
    @DisplayName("복수 정답 중 하나와 일치하면 정답을 반환한다")
    void checkAnswerMultipleAnswers() {
        Long questionIdx = 1L;

        AnswerEntity answer_1 = AnswerEntity.builder()
                .questionIdx(questionIdx)
                .answer("Spring Boot")
                .build();

        AnswerEntity answer_2 = AnswerEntity.builder()
                .questionIdx(questionIdx)
                .answer("스프링 부트")
                .build();

        when(answerRepository.findByQuestionIdx(questionIdx))
                .thenReturn(List.of(answer_1, answer_2));

        AnswerCheckResponse response = answerService.checkAnswer(
                questionIdx,
                new AnswerCheckRequest("스 프 링 부 트")
        );

        assertThat(response.correct()).isTrue();
    }

    @Test
    @DisplayName("입력한 답이 저장된 정답과 다르면 오답을 반환한다")
    void checkAnswerWrongAnswer() {
        Long questionIdx = 1L;

        AnswerEntity answer = AnswerEntity.builder()
                .questionIdx(questionIdx)
                .answer("Spring Boot")
                .build();

        when(answerRepository.findByQuestionIdx(questionIdx))
                .thenReturn(List.of(answer));

        AnswerCheckResponse response = answerService.checkAnswer(
                questionIdx,
                new AnswerCheckRequest("Java")
        );

        assertThat(response.correct()).isFalse();
    }

    @Test
    @DisplayName("입력한 답이 빈 문자열이면 오답을 반환한다")
    void checkAnswerBlankAnswer() {
        Long questionIdx = 1L;

        AnswerEntity answer = AnswerEntity.builder()
                .questionIdx(questionIdx)
                .answer("Spring Boot")
                .build();

        when(answerRepository.findByQuestionIdx(questionIdx))
                .thenReturn(List.of(answer));

        AnswerCheckResponse response = answerService.checkAnswer(
                questionIdx,
                new AnswerCheckRequest("   ")
        );

        assertThat(response.correct()).isFalse();
    }

    @Test
    @DisplayName("등록된 정답이 없으면 예외가 발생한다")
    void checkAnswerNoRegisteredAnswer() {
        Long questionIdx = 1L;

        when(answerRepository.findByQuestionIdx(questionIdx))
                .thenReturn(List.of());

        assertThatThrownBy(() -> answerService.checkAnswer(
                questionIdx,
                new AnswerCheckRequest("Spring Boot")
        ))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("해당 문제에 등록된 정답이 없습니다.");
    }
}