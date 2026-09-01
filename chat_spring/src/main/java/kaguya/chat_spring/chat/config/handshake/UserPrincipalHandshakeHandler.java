package kaguya.chat_spring.chat.config.handshake;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

// 프레임워크에서 제공하는 Handshake 기능들 상속 받기
public class UserPrincipalHandshakeHandler extends DefaultHandshakeHandler {

    // Principal(신분증) Header로 받은 userId로 세팅
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // Websocket 세션에서 userId 가져오기
        String userId = (String) attributes.get("userId");
        return () -> userId;
    }
}