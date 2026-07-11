package kaguya.chat_spring.STOMP.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kaguya.chat_spring.common.ChatPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StompSubscriber {

    private final ObjectMapper objectMapper;
    private final SimpMessageSendingOperations messagingTemplate;

    // 방, 유저 같은 Session들을 map으로 관리할 필요 없음 (STOMP 내장 브로커가 알아서 해줌)

    public void onMessage(String publishMessage, String topic) {
        try {
            // 순수 JSON 문자열 -> 자바 객체 변환
            ChatPayload chatMessage = objectMapper.readValue(publishMessage, ChatPayload.class);

            if (topic.startsWith("room:")) {
                // 방 입장 및 채팅
                String destination = "/topic/room/" + chatMessage.roomId();
                messagingTemplate.convertAndSend(destination, chatMessage);
            }
            else if (topic.equals("broadcast")) {
                // 전체 공지
                String destination = "/topic/broadcast/";
                messagingTemplate.convertAndSend(destination, chatMessage);
            }
            else if (topic.startsWith("user:")) {
                // DM
                String destination = "/queue/dm/" + chatMessage.receiver();
                messagingTemplate.convertAndSend(destination, chatMessage);
            }

        } catch (Exception e) {
            log.error("Redis 메시지 역직렬화 및 STOMP 전송 실패: ", e);
        }
    }
}
