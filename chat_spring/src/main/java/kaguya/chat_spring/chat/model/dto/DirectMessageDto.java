package kaguya.chat_spring.chat.model.dto;

public record DirectMessageDto(
        String sender,
        String receiver,
        String message
) {}