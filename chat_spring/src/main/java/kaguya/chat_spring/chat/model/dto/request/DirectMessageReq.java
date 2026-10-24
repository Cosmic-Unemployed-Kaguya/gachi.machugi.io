package kaguya.chat_spring.chat.model.dto.request;

public record DirectMessageReq(
        String receiver,
        String message
) {}