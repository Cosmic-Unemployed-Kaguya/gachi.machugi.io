package kaguya.domain.answer.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kaguya.domain.answer.model.dto.request.AnswerRequest;
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

    // 정답 입력
}