package kaguya.chat_spring.websocket.chat.model;

public record ChatPayload(
        MessageType type,  // ENTER, TALK, BROADCAST, DM
        String roomId,
        String sender,  // 보내는 사람 닉네임 또는 ID
        String receiver,  // DM 용
        String message  // 내용
) {
    public enum MessageType {
        ENTER,      // 방 입장
        TALK,       // 방 대화
        BROADCAST,  // 전체 공지
        DM          // 귓속말
    }
}