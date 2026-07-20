package kaguya.chat_spring.chat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kaguya.chat_spring.chat.common.RedisPublisher;
import kaguya.chat_spring.chat.model.BroadcastDto;
import kaguya.chat_spring.chat.model.DirectMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

/**
 * @MessageMapping으로 프론트가 보낸 목적지 주소('/app/...') 가로채기
 * @PostMapping 이라고 이해하면 편함
 */
@Controller
@RequiredArgsConstructor
public class GlobalController {

    private final ObjectMapper objectMapper;
    private final RedisPublisher redisPublisher;  // Redis Publisher

    /**
     * 전체 공지 (Broadcast)
     * 프론트엔드 전송 목적지: /app/chat.broadcast
     * 프론트엔드 전송 데이터: { "sender": "운영자", "message": "서버 점검 안내" }
     */
    @MessageMapping("/chat.broadcast")
    public void broadcast(BroadcastDto broadcastDto) throws JsonProcessingException {

        String topic = "chat:broadcast";
        String message = objectMapper.writeValueAsString(broadcastDto);

        redisPublisher.publish(topic, message);
    }

    /**
     * 귓속말 (DM)
     * 프론트엔드 전송 목적지: /app/chat.dm
     * 프론트엔드 전송 데이터: { "sender": "user1", "receiver": "user2", "message": "비밀이야" }
     */
    @MessageMapping("/chat.dm")
    public void directMessage(DirectMessageDto directMessageDto) throws JsonProcessingException {

        DirectMessageDto dm = new DirectMessageDto(
                directMessageDto.sender(),
                directMessageDto.receiver(),
                directMessageDto.message()
        );

        String topic = "chat:dm:" + directMessageDto.receiver();
        String message = objectMapper.writeValueAsString(dm);

        redisPublisher.publish(topic, message);
    }
}