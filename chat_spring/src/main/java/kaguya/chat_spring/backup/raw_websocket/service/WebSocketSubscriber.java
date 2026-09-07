package kaguya.chat_spring.backup.raw_websocket.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kaguya.chat_spring.backup.raw_websocket.common.ChatPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
//@Service
@RequiredArgsConstructor
public class WebSocketSubscriber {

    private final ObjectMapper objectMapper;
    private final WebSocketChatService chatService;

    public void onMessage(String message) {
        try {
            // 순수 JSON 문자열 -> 자바 객체 변환
            ChatPayload payload = objectMapper.readValue(message, ChatPayload.class);

            switch (payload.type()) {
                case ENTER:

                case TALK:
                    chatService.sendToLocalRoom(payload.roomId(), payload);
                    break;

                case BROADCAST:
                    chatService.sendToAllLocalUsers(payload);
                    break;

                case DM:
                    chatService.sendToLocalUser(payload.receiver(), payload);
                    break;
            }
        } catch (Exception e) {
            log.error("Redis 메시지 파싱 및 전송 실패: {}", message, e);
        }
    }
}