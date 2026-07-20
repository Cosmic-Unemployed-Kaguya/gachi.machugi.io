package kaguya.chat_spring.chat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kaguya.chat_spring.backup.raw_websocket.common.ChatPayload;
import kaguya.chat_spring.chat.common.RedisPublisher;
import kaguya.chat_spring.chat.model.DirectMessageDto;
import kaguya.chat_spring.chat.model.LobbyDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

/**
 * @MessageMapping으로 프론트가 보낸 목적지 주소('/app/...') 가로채기
 * @PostMapping 이라고 이해하면 편함
 */
@Controller
@RequiredArgsConstructor
public class LobbyController {

    private final ObjectMapper objectMapper;
    private final RedisPublisher redisPublisher;  // Redis Publisher

    /**
     * 로비(Lobby) 입장
     * 프론트엔드 전송 목적지: /app/chat.lobby.enter
     */
    @MessageMapping("/chat.lobby.enter")
    public void enter(DirectMessageDto directMessageDto) throws JsonProcessingException {

        DirectMessageDto enter = new DirectMessageDto(
                "[System]",
                directMessageDto.receiver(),
                "로비에 입장하였습니다."
        );

        // 발행할 topic 설정
        String topic = "chat:dm:" + directMessageDto.receiver();
        // Redis로 발행하기 위한 json을 String 타입으로 변경
        String message = objectMapper.writeValueAsString(enter);

        redisPublisher.publish(topic, message);
    }

    /**
     * 로비(Lobby) 대화
     * 프론트엔드 전송 목적지: /app/chat.lobby
     * 프론트엔드 전송 데이터: { "sender": "user1", "message": "안녕하세요" }
     */
    @MessageMapping("/chat.lobby.talk")
    public void talk(LobbyDto lobbyDto) throws JsonProcessingException {

        String topic = "chat:lobby";
        String message = objectMapper.writeValueAsString(lobbyDto);

        redisPublisher.publish(topic, message);
    }
}