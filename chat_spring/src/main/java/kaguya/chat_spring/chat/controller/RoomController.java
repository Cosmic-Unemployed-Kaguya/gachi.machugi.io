package kaguya.chat_spring.chat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kaguya.chat_spring.chat.common.RedisPublisher;
import kaguya.chat_spring.chat.model.dto.RoomMessageDto;
import kaguya.chat_spring.chat.model.dto.request.TalkReq;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class RoomController {

    private final ObjectMapper objectMapper;
    private final RedisPublisher redisPublisher;  // Redis Publisher

    /**
     * 방(Room) 입장
     * 프론트엔드 전송 목적지: /pub/chat/room/{roomId}/enter
     */
    @MessageMapping("/chat/room/{roomId}/enter")
    public void enter(@DestinationVariable String roomId, SimpMessageHeaderAccessor headerAccessor) throws JsonProcessingException {

        Map<String, Object> attributes = headerAccessor.getSessionAttributes();
        String userId = (String) attributes.get("userId");

        RoomMessageDto system = new RoomMessageDto(
                roomId,
                "[System]",
                userId + "님이 방에 입장했습니다."
        );

        // 발행할 topic 설정
        String topic = "chat:room:" + roomId;
        // Redis로 발행하기 위한 json을 String 타입으로 변경
        String message = objectMapper.writeValueAsString(system);

        redisPublisher.publish(topic, message);
    }

    /**
     * 방(Room) 대화
     * 프론트엔드 전송 목적지: /pub/chat/room/{roomId}/talk
     * 프론트엔드 전송 데이터: { "message": "안녕하세요" }
     */
    @MessageMapping("/chat/room/{roomId}/talk")
    public void talk(@DestinationVariable String roomId, SimpMessageHeaderAccessor headerAccessor, TalkReq talkReq) throws JsonProcessingException {

        Map<String, Object> attributes = headerAccessor.getSessionAttributes();
        String nickname = (String) attributes.get("nickname");

        RoomMessageDto room = new RoomMessageDto(
                roomId,
                nickname,
                talkReq.message()
        );

        String topic = "chat:room:" + roomId;
        String message = objectMapper.writeValueAsString(room);

        redisPublisher.publish(topic, message);
    }
}