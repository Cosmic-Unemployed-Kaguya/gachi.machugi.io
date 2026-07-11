package kaguya.chat_spring.STOMP.model;

public record ChatDto(
        // type 필요 없음
        String roomId,
        String sender,
        String receiver,
        String message
) {}