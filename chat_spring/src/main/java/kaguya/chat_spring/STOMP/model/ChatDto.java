package kaguya.chat_spring.STOMP.model;

public record ChatDto(
        // type 필요 없음
        String roomId,
        String sender,
        String receiver,
        String message
) {
    /**
     * 서버에서 URL 경로로 알아낸 roomId를 DTO에 강제로 주입하여
     * 프론트엔드에게 돌려주기 위한 메서드 (프론트는 roomId 관리 안해도 됨)
     */
    public ChatDto withRoomId(String roomId) {
        return new ChatDto(roomId, this.sender, this.receiver, this.message);
    }
}