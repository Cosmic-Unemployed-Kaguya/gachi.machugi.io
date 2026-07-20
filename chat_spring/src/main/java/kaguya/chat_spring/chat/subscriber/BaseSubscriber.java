package kaguya.chat_spring.chat.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseSubscriber<T> {

    private final ObjectMapper objectMapper;
    private final SimpMessageSendingOperations messagingTemplate;
    private final Class<T> type;

    // 방, 유저 같은 Session들을 map으로 관리할 필요 없음 (STOMP 내장 브로커가 알아서 해줌)

    public void onMessage(String publishMessage, String topic) {
        try {
            T dto = objectMapper.readValue(publishMessage, type);
            String destination = getDestination(topic, dto);
            messagingTemplate.convertAndSend(destination, dto);
        } catch (Exception e) {
            log.error("Redis 메시지 역직렬화 및 STOMP 전송 실패: ", e);
        }
    }

    protected abstract String getDestination(String topic, T dto);
}
