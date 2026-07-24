package kaguya.chat_spring.chat.config;

import jakarta.servlet.http.HttpServletRequest;
import kaguya.chat_spring.chat.common.RedisRepository;
import kaguya.chat_spring.chat.model.enums.Role;
import kaguya.chat_spring.chat.service.UserGrpcClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    private final RedisRepository redisRepository;
    private final UserGrpcClient userGrpcClient;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        if (request instanceof ServletServerHttpRequest servletHttpRequest) {
            HttpServletRequest req = servletHttpRequest.getServletRequest();

            String userId = req.getHeader("x-user-id");
            String userRole = req.getHeader("x-user-role");
            String nickname = "";

            if (userId == null || userRole == null) {
                log.warn("웹소켓 연결 실패: 인증 헤더 누락");
                return false;
            }

            if (Role.USER.name().equals(userRole)) {
                try {
                    nickname = userGrpcClient.getNickname(userId);
                } catch (Exception e) {
                    log.error("user 서비스 통신간 에러 발생: {}", e.getMessage());
                }

            }
            else if (Role.GUEST.name().equals(userRole)) {
                nickname = redisRepository.get("GUSET:" + userId);
            }

            attributes.put("userId", userId);
            attributes.put("userRole", userRole);
            attributes.put("nickname", nickname);

            log.info("웹소켓 핸드셰이크 성공 (userId={})", userId);

            return true;
        }

        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {

        // 핸드셰이크 완료 후 처리할 로직

    }
}
