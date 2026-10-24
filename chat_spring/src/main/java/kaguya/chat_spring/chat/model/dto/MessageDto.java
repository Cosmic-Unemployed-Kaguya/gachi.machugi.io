package kaguya.chat_spring.chat.model.dto;

public record MessageDto(
        String sender,
        String message
) {}