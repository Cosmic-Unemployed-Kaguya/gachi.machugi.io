package kaguya.user.domain.common.controller;

import jakarta.validation.Valid;
import kaguya.user.domain.common.model.dto.BaseRes;
import kaguya.user.domain.common.model.dto.request.CheckVerificationCodeReq;
import kaguya.user.domain.common.model.dto.request.SendVerificationCodeReq;
import kaguya.user.domain.common.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/verifications")
public class VerificationController {

    private final VerificationService verificationService;

    /**
     * [1단계] 인증코드를 지정한 메일로 전송
     * @param request: 사용자 이메일
     */
    @PostMapping("/send")
    public ResponseEntity<BaseRes<Void>> sendVerificationCode(
            @RequestBody @Valid SendVerificationCodeReq request
    ) {
        verificationService.sendVerificationCode(request.email());

        return ResponseEntity.ok(
                new BaseRes<>("200", "인증코드 전송 완료. 메일을 확인하세요.", null)
        );
    }

    /**
     * [2단계] 인증코드가 맞는지 체크 후 일회용 인증코드 return
     * @param request: 이메일, 인증 코드
     */
    @PostMapping("/check")
    public ResponseEntity<BaseRes<String>> checkVerificationCode(
            @RequestBody @Valid CheckVerificationCodeReq request
    ) {
        String oneTimeAuthCode = verificationService.checkVerificationCode(request);

        return ResponseEntity.ok(
                new BaseRes<>("200", "인증 완료", oneTimeAuthCode)
        );
    }

    // [3단계] 일회용 인증코드 확인 후 맞으면 서비스(ex. 아이디 찾기, 비밀번호 변경 등) 동작
}