package kaguya.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

// RestController 보고 있다가 예외가 터지면 가로채기
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 비즈니스 로직에서 던진 BusinessException 처리
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorRes> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        ErrorRes response = new ErrorRes(errorCode);  // 에러 응답 객체

        return new ResponseEntity<>(response, errorCode.getStatus());
    }

    // @Valid 검증 실패 시 발생하는 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorRes> handleValidationException(MethodArgumentNotValidException e) {

        // 발생한 에러 중 첫 번째 에러의 메시지를 가져옴 (ex. "아이디는 필수입니다.")
        String errorMessage = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;  // 400 Bad Request

        // record의 생성자를 사용해 커스텀 메시지를 직접 설정
        ErrorRes response = new ErrorRes(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorMessage
        );

        return new ResponseEntity<>(response, errorCode.getStatus());
    }

    // 존재하지 않는 API 주소 요청 시
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    protected ResponseEntity<ErrorRes> handleNotFoundException(Exception e) {

        ErrorCode errorCode = ErrorCode.PAGE_NOT_FOUND;  // 404 Not Found
        ErrorRes response = new ErrorRes(errorCode);

        return new ResponseEntity<>(response, errorCode.getStatus());
    }

    // 그 외 처리하지 못한 모든 예외 처리
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorRes> handleException(Exception e) {

        // 무슨 문제인지 로그 확인
        log.error("서버 내부 에러 발생: ", e);

        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;  // 500 Internal Server Error
        ErrorRes response = new ErrorRes(errorCode);

        return new ResponseEntity<>(response, errorCode.getStatus());
    }

}
