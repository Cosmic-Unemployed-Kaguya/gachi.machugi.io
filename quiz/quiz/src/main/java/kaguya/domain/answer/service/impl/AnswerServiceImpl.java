package kaguya.domain.answer.service.impl;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kaguya.domain.answer.model.dto.request.AnswerCheckRequest;
import kaguya.domain.answer.model.dto.request.AnswerRequest;
import kaguya.domain.answer.model.dto.response.AnswerCheckResponse;
import kaguya.domain.answer.model.dto.response.AnswerResponse;
import kaguya.domain.answer.model.entity.AnswerEntity;
import kaguya.domain.answer.repository.AnswerRepository;
import kaguya.domain.answer.service.AnswerService;
import kaguya.domain.answer.util.mapper.AnswerMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AnswerServiceImpl implements AnswerService {

    private final AnswerRepository answerRepository;
    private final AnswerMapper answerMapper;

    // 정답 생성
    @Override
    public AnswerResponse createAnswer(Long questionIdx, AnswerRequest request) {
        AnswerEntity answer = answerMapper.requestToEntity(questionIdx, request);
        AnswerEntity savedAnswer = answerRepository.save(answer);

        return answerMapper.entityToResponse(savedAnswer);
    }

    // 정답 조회
    @Override
    @Transactional(readOnly = true)
    public List<AnswerResponse> getAnswerList(Long questionIdx) {
        return answerRepository.findByQuestionIdx(questionIdx)
                .stream()
                .map(answerMapper::entityToResponse)
                .toList();
    }

    // 정답 수정
    @Override
    public AnswerResponse patchAnswer(Long answerIdx, AnswerRequest request) {
        AnswerEntity answer = answerRepository.findById(answerIdx)
                .orElseThrow(() -> new RuntimeException("정답이 존재하지 않습니다."));

        answer.patchAnswer(request.answer());

        return answerMapper.entityToResponse(answer);
    }

    // 정답 삭제
    @Override
    public void deleteAnswer(Long answerIdx) {
        AnswerEntity answer = answerRepository.findById(answerIdx)
                .orElseThrow(() -> new RuntimeException("정답이 존재하지 않습니다."));

        answerRepository.delete(answer);
    }

    // 정답 체크
    @Override
    @Transactional(readOnly = true)
    public AnswerCheckResponse checkAnswer(
            Long questionIdx,
            AnswerCheckRequest request
    ) {
        List<AnswerEntity> answerList =
                answerRepository.findByQuestionIdx(questionIdx);

        if (answerList.isEmpty()) {
            throw new RuntimeException(
                    "해당 문제에 등록된 정답이 없습니다."
            );
        }

        String inputAnswer = normalizeAnswer(request.answer());

        if (inputAnswer == null || inputAnswer.isBlank()) {
            return new AnswerCheckResponse(
                    questionIdx,
                    false
            );
        }

        boolean correct = answerList.stream()
                .map(AnswerEntity::getAnswer)
                .map(this::normalizeAnswer)
                .anyMatch(inputAnswer::equals);

        return new AnswerCheckResponse(
                questionIdx,
                correct
        );
    }

    // 정답을 비교하기 위해 입력값과 저장된 정답을 모두 소문자로 변환하고, 공백을 제거하는 메서드
    private String normalizeAnswer(String answer) {
        if (answer == null) {
            return null;
        }

        // 모든 공백 제거 후 소문자로 변환
        return answer.replaceAll("\\s+", "")
                .toLowerCase(Locale.ROOT);
        // return answer.strip()
        //         .toLowerCase(Locale.ROOT);
    }
}