package kaguya.chat_spring.chat.model.dto.request;

public record EnterLobbyReq(
        String username,
        String nickname
) {}