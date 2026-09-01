package kaguya.chat_spring.chat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kaguya.chat_spring.chat.common.RedisPublisher;
import kaguya.chat_spring.chat.model.dto.DirectMessageDto;
import kaguya.chat_spring.chat.model.dto.MessageDto;
import kaguya.chat_spring.chat.model.dto.request.TalkReq;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class LobbyController {

    private final ObjectMapper objectMapper;
    private final RedisPublisher redisPublisher;  // Redis Publisher

    /**
     * 로비(Lobby) 입장
     * 프론트엔드 전송 목적지: /pub/chat/lobby/enter
     */
    @MessageMapping("/chat/lobby/enter")
    public void enter(SimpMessageHeaderAccessor headerAccessor) throws JsonProcessingException {

        Map<String, Object> attributes = headerAccessor.getSessionAttributes();
        String userId = (String) attributes.get("userId");

        DirectMessageDto enter = new DirectMessageDto(
                "[System]",
                userId,
                "로비에 입장하였습니다."
        );

        // 발행할 topic 설정
        String topic = "chat:dm:" + userId;
        // Redis로 발행하기 위한 json을 String 타입으로 변경
        String message = objectMapper.writeValueAsString(enter);

        redisPublisher.publish(topic, message);
    }

    /**
     * 로비(Lobby) 대화
     * 프론트엔드 전송 목적지: /pub/chat/lobby/talk
     * 프론트엔드 전송 데이터: { "message": "안녕하세요" }
     */
    @MessageMapping("/chat/lobby/talk")
    public void talk(SimpMessageHeaderAccessor headerAccessor, TalkReq talkReq) throws JsonProcessingException {

        Map<String, Object> attributes = headerAccessor.getSessionAttributes();
        String nickname = (String) attributes.get("nickname");

        MessageDto messageDto = new MessageDto(
                nickname,
                talkReq.message()
        );

        String topic = "chat:lobby";
        String message = objectMapper.writeValueAsString(messageDto);

        redisPublisher.publish(topic, message);
    }
}