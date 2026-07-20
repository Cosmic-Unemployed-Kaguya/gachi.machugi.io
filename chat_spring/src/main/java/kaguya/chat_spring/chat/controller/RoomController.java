package kaguya.chat_spring.chat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kaguya.chat_spring.chat.model.LobbyDto;
import kaguya.chat_spring.backup.raw_websocket.common.ChatPayload;
import kaguya.chat_spring.chat.common.RedisPublisher;
import kaguya.chat_spring.chat.model.RoomDto;
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
public class RoomController {

    private final ObjectMapper objectMapper;
    private final RedisPublisher redisPublisher;  // Redis Publisher

    /**
     * 방(Room) 입장
     * 프론트엔드 전송 목적지: /app/chat.room.{roomId}.enter
     * 프론트엔드 전송 데이터: { "sender": "user1" }
     */
    @MessageMapping("/chat.room.{roomId}.enter")
    public void enter(@DestinationVariable String roomId, RoomDto roomDto) throws JsonProcessingException {

        RoomDto system = new RoomDto(
                roomId,
                "[System]",
                roomDto.sender() + "님이 방에 입장했습니다."
        );

        // 발행할 topic 설정
        String topic = "chat:room:" + roomId;
        // Redis로 발행하기 위한 json을 String 타입으로 변경
        String message = objectMapper.writeValueAsString(system);

        redisPublisher.publish(topic, message);
    }

    /**
     * 방(Room) 대화
     * 프론트엔드 전송 목적지: /app/chat.room.{roomId}.talk
     * 프론트엔드 전송 데이터: { "sender": "user1", "message": "안녕하세요" }
     */
    @MessageMapping("/chat.room.{roomId}.talk")
    public void talk(@DestinationVariable String roomId, RoomDto roomDto) throws JsonProcessingException {

        RoomDto room = new RoomDto(
                roomId,
                roomDto.sender(),
                roomDto.message()
        );

        String topic = "chat:room:" + roomId;
        String message = objectMapper.writeValueAsString(room);

        redisPublisher.publish(topic, message);
    }
}