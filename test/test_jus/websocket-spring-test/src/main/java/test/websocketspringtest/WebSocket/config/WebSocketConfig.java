package test.websocketspringtest.WebSocket.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatHandler chatHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 클라이언트가 'ws://서버주소/ws/raw' 로 접속하면 핸들러가 작동하도록 매핑
        registry.addHandler(chatHandler, "/ws/raw")
                .setAllowedOrigins("*"); // 실무에서는 CORS 설정 필요
    }
}
