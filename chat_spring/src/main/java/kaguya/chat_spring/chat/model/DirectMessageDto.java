package kaguya.chat_spring.chat.model;

public record DirectMessageDto(
        String sender,
        String receiver,
        String message
) {}