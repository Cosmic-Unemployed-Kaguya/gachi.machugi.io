package kaguya.chat_spring.chat.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

@Slf4j
@ControllerAdvice
public class GlobalWebSocketExceptionHandler {

    // 웹소켓 처리 중 터진 exception
    @SendToUser("/queue/errors")
    @MessageExceptionHandler(WebSocketException.class)
    public ErrorRes handleWebSocketException(WebSocketException e) {

        ErrorCode errorCode = e.getErrorCode();
        return new ErrorRes(errorCode);
    }

    // 그 외 알수 없는 exception
    @SendToUser("/queue/errors")
    @MessageExceptionHandler(Exception.class)
    public ErrorRes handleException(Exception e) {

        // 무슨 문제인지 로그 확인
        log.error("서버 내부 에러 발생: ", e);

        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;  // 500 Internal Server Error

        return new ErrorRes(errorCode);
    }
}
