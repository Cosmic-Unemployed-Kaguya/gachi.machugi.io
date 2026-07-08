package kaguya.chat_spring.STOMP.controller;

import kaguya.chat_spring.STOMP.model.ChatDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

/**
 * @MessageMapping으로 프론트가 보낸 목적지 주소('/app/...') 가로채기
 * @PostMapping 이라고 이해하면 편함
 */
@Controller
@RequiredArgsConstructor
public class ChatController {
    // 메시지 발송 전용 도구
    // 직접 for문을 돌며 세션을 찾을 필요 없이, 목적지만 적어주면 알아서 전송해 줌
    private final SimpMessageSendingOperations messagingTemplate;

    /**
     * 방(Room) 입장
     * 프론트엔드 전송 목적지: /app/chat.room.{roomId}.enter
     * 프론트엔드 전송 데이터: { "sender": "user1" }
     */
    @MessageMapping("/chat.room.{roomId}.enter")
    public void enterRoom(@DestinationVariable String roomId, ChatDto payload) {
        // 프론트에서 넘겨준 sender 닉네임을 활용해 시스템 알림 메시지를 직접 생성
        ChatDto systemNotice = new ChatDto(
                roomId,
                "SYSTEM",
                null,
                payload.sender() + "님이 입장하셨습니다."
        );

        // 객체를 JSON으로 자동 변환(Convert)해서, 해당 방 구독자들(/topic/room/{roomId})에게 전송
        messagingTemplate.convertAndSend("/topic/room/" + roomId, systemNotice);
    }

    /**
     * 방(Room) 대화
     * 프론트엔드 전송 목적지: /app/chat.room.{roomId}.talk
     * 프론트엔드 전송 데이터: { "sender": "user1", "message": "안녕하세요" }
     */
    @MessageMapping("/chat.room.{roomId}.talk")
    public void talkRoom(@DestinationVariable String roomId, ChatDto payload) {
        // 프론트가 비워둔 roomId를 서버가 URL 경로에서 가져와 다시 세팅
        ChatDto completePayload = payload.withRoomId(roomId);

        // JSON으로 변환한 메시지를 브로커에게 던져서 해당 방에 있는 모든 구독자에게 전송
        messagingTemplate.convertAndSend("/topic/room/" + roomId, completePayload);
    }

    /**
     * 전체 공지 (Broadcast)
     * 프론트엔드 전송 목적지: /app/chat.broadcast
     * 프론트엔드 전송 데이터: { "sender": "운영자", "message": "서버 점검 안내" }
     */
    @MessageMapping("/chat.broadcast")
    public void handleBroadcast(ChatDto payload) {
        // 브로드캐스트는 특정 방에 속한 것이 아니므로 roomId 조작 없이 그대로 전송
        messagingTemplate.convertAndSend("/topic/broadcast", payload);
    }

    /**
     * 귓속말 (DM)
     * 프론트엔드 전송 목적지: /app/chat.dm
     * 프론트엔드 전송 데이터: { "sender": "user1", "receiver": "user2", "message": "비밀이야" }
     */
    @MessageMapping("/chat.dm")
    public void handleDM(ChatDto payload) {
        String receiver = payload.receiver();

        // 수신자의 개인 큐(/queue/dm/수신자닉네임)로 메시지를 전송
        messagingTemplate.convertAndSend("/queue/dm/" + receiver, payload);
    }
}