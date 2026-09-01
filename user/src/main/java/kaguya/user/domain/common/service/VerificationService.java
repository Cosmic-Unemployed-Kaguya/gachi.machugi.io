package kaguya.user.domain.common.service;

import kaguya.user.domain.common.model.dto.request.CheckVerificationCodeReq;
import kaguya.user.domain.common.model.dto.request.SendVerificationCodeReq;
import kaguya.user.domain.common.repository.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final RedisRepository redisRepository;
    private final MailService mailService;

    // 난수 생성
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int CODE_LENGTH = 6;

    public void sendVerificationCode(SendVerificationCodeReq request) {

        String type = request.verificationType().name();
        String email = request.email();

        // 카운트 증가 후 시도횟수 확인
        String limitKey = "verification:limit:" + type + ":" + email;
        Long currentCount = redisRepository.increment(limitKey, 2, TimeUnit.HOURS);

        if (currentCount > 5) {
            // todo. 커스텀 Exception으로 변경
            throw new RuntimeException("요청 횟수 초과");
        }

        // 인증코드 생성
        String verificationCode = createVerificationCode();

        String subject = "[같이맞추기.IO] 인증코드 전송 메일입니다.";
        String content = "<h1> 인증코드는 [ " + verificationCode + " ] 입니다.</h1>";

        // redis에 이메일 인증코드 저장
        String verificationKey = "verification:code:" + type + ":" + email;
        redisRepository.save(verificationKey, verificationCode, 10, TimeUnit.MINUTES);

        // 메일 전송
        mailService.sendHtmlMail(email, subject, content);
    }

    public String checkVerificationCode(CheckVerificationCodeReq request) {

        String type = request.verificationType().name();
        String userCode = request.verificationCode();
        String email = request.email();

        // 검증코드 조회 및 확인
        String verificationKey = "verification:code:" + type + ":" + email;
        String serverCode = redisRepository.get(verificationKey);

        if (!userCode.equals(serverCode)) {
            // todo. 커스텀 Exception으로 변경
            throw new RuntimeException("잘못된 인증코드 입니다.");
        }
        redisRepository.delete(email);

        // 추후 작업(3단계)을 위한 일회용 인증코드 생성 및 Redis 저장
        String oneTimeAuthCode = UUID.randomUUID().toString();
        String oneTimeKey = "verification:oneTimeAuthCode:" + type + ":" + oneTimeAuthCode;
        redisRepository.save(oneTimeKey, email, 10, TimeUnit.MINUTES);

        // 일회용 인증코드 반환
        return oneTimeAuthCode;
    }

    // 영문·숫자 조합의 6자리 인증코드 생성
    private String createVerificationCode() {

        StringBuilder code = new StringBuilder(CODE_LENGTH);

        while (code.length() < CODE_LENGTH) {
            // 48 이상 123 미만(48~122)의 난수 뽑기
            // 공식: nextInt(최대값 - 최소값 + 1) + 최소값
            int randomInt = SECURE_RANDOM.nextInt(122 - 48 + 1) + 48;

            // 아스키코드 범위
            boolean isNumber = (randomInt >= 48 && randomInt <= 57);
            boolean isUpperCase = (randomInt >= 65 && randomInt <= 90);
            boolean isLowerCase = (randomInt >= 97 && randomInt <= 122);

            // 숫자, 대문자, 소문자 중 하나에 해당하면 문자열에 추가
            if (isNumber || isUpperCase || isLowerCase) {
                code.append((char) randomInt);
            }
        }

        return code.toString();
    }
}