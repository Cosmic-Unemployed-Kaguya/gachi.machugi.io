package kaguya.chat_spring.chat.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseSubscriber<T> {

    private final ObjectMapper objectMapper;
    // Redis에서 수신한 메시지를 STOMP 브로커로 전달하기 위한 전송 객체
    private final SimpMessageSendingOperations messagingTemplate;
    private final Class<T> type;

    // 방, 유저 같은 Session들을 map으로 관리할 필요 없음 (STOMP 내장 브로커가 알아서 해줌)

    /**
     * MessageListenerAdapter가 Redis 메시지를 수신했을 때 실행하는 콜백 메서드
     * @param publishMessage: Redis 통해 전달된 직렬화된 데이터(JSON 문자열)
     * @param topic: 메시지가 발행된 Redis 토픽
     */
    public void onMessage(String publishMessage, String topic) {
        try {
            // JSON 문자열을 자바 객체(dto)로 역직렬화
            T dto = objectMapper.readValue(publishMessage, type);
            // 목적지 경로 추출
            String destination = getDestination(topic, dto);

            // 직렬화 후 스프링 내부의 STOMP 브로커에 데이터 전송
            messagingTemplate.convertAndSend(destination, dto);

        } catch (Exception e) {
            log.error("Redis 메시지 역직렬화 및 STOMP 전송 실패: ", e);
        }
    }

    // STOMP 메시지를 전송할 웹소켓 목적지 경로 추출
    protected abstract String getDestination(String topic, T dto);
}