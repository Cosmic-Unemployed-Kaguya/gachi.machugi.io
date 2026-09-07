package kaguya.chat_spring.chat.model.dto;

public record RoomMessageDto(
        String roomId,
        String sender,
        String message
) {}