package kaguya.chat_spring.chat.model;

public record RoomDto(
        String roomId,
        String sender,
        String message
) {}