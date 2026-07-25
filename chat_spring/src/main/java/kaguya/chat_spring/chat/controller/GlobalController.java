package kaguya.chat_spring.chat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kaguya.chat_spring.chat.common.RedisPublisher;
import kaguya.chat_spring.chat.model.dto.DirectMessageDto;
import kaguya.chat_spring.chat.model.dto.request.DirectMessageReq;
import kaguya.chat_spring.chat.model.dto.request.TalkReq;
import kaguya.chat_spring.chat.model.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class GlobalController {

    private final ObjectMapper objectMapper;
    private final RedisPublisher redisPublisher;  // Redis Publisher

    /**
     * 전체 공지 (Broadcast)
     * 프론트엔드 전송 목적지: /pub/chat/broadcast
     * 프론트엔드 전송 데이터: { "sender": "운영자", "message": "서버 점검 안내" }
     */
    @MessageMapping("/chat/broadcast")
    public void broadcast(TalkReq talkReq) throws JsonProcessingException {

        String topic = "chat:broadcast";
        String message = objectMapper.writeValueAsString(talkReq);

        redisPublisher.publish(topic, message);
    }

    /**
     * 귓속말 (DM) - 유저만 가능
     * 프론트엔드 전송 목적지: /pub/chat/dm
     * 프론트엔드 전송 데이터: { "sender": "user1", "receiver": "user2", "message": "비밀이야" }
     */
    @MessageMapping("/chat/dm")
    public void directMessage(SimpMessageHeaderAccessor headerAccessor, DirectMessageReq directMessageReq) throws JsonProcessingException {

        Map<String, Object> attributes = headerAccessor.getSessionAttributes();
        String role = (String) attributes.get("role");
        if (Role.GUEST.name().equals(role)) {
            // 게스트 유저는 DM 못보냄
            return;
        }

        String nickname = (String) attributes.get("nickname");

        DirectMessageDto dm = new DirectMessageDto(
                nickname,
                directMessageReq.receiver(),
                directMessageReq.message()
        );

        String topic = "chat:dm:" + directMessageReq.receiver();
        String message = objectMapper.writeValueAsString(dm);

        redisPublisher.publish(topic, message);
    }
}