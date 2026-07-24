package kaguya.chat_spring.chat.model.dto;

public record RoomDto(
        String roomId,
        String sender,
        String message
) {}