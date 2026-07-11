package kaguya.chat_spring.STOMP.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kaguya.chat_spring.STOMP.model.ChatDto;
import kaguya.chat_spring.common.ChatPayload;
import kaguya.chat_spring.common.RedisPublisher;
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
public class ChatController {

    private final ObjectMapper objectMapper;
    private final RedisPublisher redisPublisher;  // Redis Publisher

    /**
     * 방(Room) 입장
     * 프론트엔드 전송 목적지: /app/chat.room.{roomId}.enter
     * 프론트엔드 전송 데이터: { "sender": "user1" }
     */
    @MessageMapping("/chat.room.{roomId}.enter")
    public void enterRoom(@DestinationVariable String roomId, ChatDto chatDto) throws Exception {

        // Redis 리스너가 이해할 수 있는 ChatPayload로 생성
        ChatPayload systemNotice = new ChatPayload(
                ChatPayload.MessageType.ENTER,
                roomId,
                "SYSTEM",
                null,
                chatDto.sender() + "님이 입장하셨습니다."
        );

        // 발행할 topic 설정
        String topic = "room:" + roomId;
        // Redis로 발행하기 위한 json을 String 타입으로 변경
        String message = objectMapper.writeValueAsString(systemNotice);

        redisPublisher.publish(topic, message);
    }

    /**
     * 방(Room) 대화
     * 프론트엔드 전송 목적지: /app/chat.room.{roomId}.talk
     * 프론트엔드 전송 데이터: { "sender": "user1", "message": "안녕하세요" }
     */
    @MessageMapping("/chat.room.{roomId}.talk")
    public void talkRoom(@DestinationVariable String roomId, ChatDto chatDto) throws Exception {

        ChatPayload payload = new ChatPayload(
                ChatPayload.MessageType.TALK,
                roomId,
                chatDto.sender(),
                null,
                chatDto.message()
        );

        String topic = "room:" + roomId;
        String message = objectMapper.writeValueAsString(payload);

        redisPublisher.publish(topic, message);
    }

    /**
     * 전체 공지 (Broadcast)
     * 프론트엔드 전송 목적지: /app/chat.broadcast
     * 프론트엔드 전송 데이터: { "sender": "운영자", "message": "서버 점검 안내" }
     */
    @MessageMapping("/chat.broadcast")
    public void handleBroadcast(ChatDto chatDto) throws Exception {

        ChatPayload payload = new ChatPayload(
                ChatPayload.MessageType.BROADCAST, // 주입
                null,
                chatDto.sender(),
                null,
                chatDto.message()
        );

        String topic = "broadcast";
        String message = objectMapper.writeValueAsString(payload);

        redisPublisher.publish(topic, message);
    }

    /**
     * 귓속말 (DM)
     * 프론트엔드 전송 목적지: /app/chat.dm
     * 프론트엔드 전송 데이터: { "sender": "user1", "receiver": "user2", "message": "비밀이야" }
     */
    @MessageMapping("/chat.dm")
    public void handleDM(ChatDto chatDto) throws Exception {

        ChatPayload payload = new ChatPayload(
                ChatPayload.MessageType.DM, // 주입
                null,
                chatDto.sender(),
                chatDto.receiver(),
                chatDto.message()
        );

        // 수신자 가져오기
        String topic = "user:" + payload.receiver();
        String message = objectMapper.writeValueAsString(payload);

        redisPublisher.publish(topic, message);
    }
}