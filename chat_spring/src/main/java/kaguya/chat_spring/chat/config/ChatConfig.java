package kaguya.chat_spring.chat.config;

import kaguya.chat_spring.chat.config.handshake.AuthHandshakeInterceptor;
import kaguya.chat_spring.chat.config.handshake.UserPrincipalHandshakeHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class ChatConfig implements WebSocketMessageBrokerConfigurer {

    private final AuthHandshakeInterceptor authHandshakeInterceptor;

    /**
     * 클라이언트가 웹소켓에 연결할 때 설정할 엔드포인트 주소 설정
     * @param registry: 연결용 주소(출입문)를 만들어 주는 객체
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 프론트엔드가 최초로 웹소켓 연결을 맺을 엔드포인트 주소 (ws://localhost:8080/ws-chat)
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*")   // 테스트용 (원래는 특정 도메인만 허용)
                .addInterceptors(authHandshakeInterceptor)  // Handshake 인터셉터
                .setHandshakeHandler(new UserPrincipalHandshakeHandler())  // HandshakeHandler
                .withSockJS();
    }

    /**
     * 브로커 설정
     * - 따로 session을 관리하지 않아도 configureMessageBroker가 알아서 세션 관리해줌
     * - 커스텀 브로커를 사용해도 브로커에 대한 설정 로직을 짤 필요 없고 간단한 코드 한줄이면 해결
     * @param registry: 메시지가 서버 내부에서 어디로 향할지 목적지(라우팅)를 설정해 주는 객체
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
/*
        // 만약 내장 브로커가 아닌 커스텀 브로커를 사용한다면 (ex. RabbitMQ)
        registry.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost("192.168.0.10") // RabbitMQ 서버 IP
                .setRelayPort(61613)  // STOMP 기본 포트
                .setClientLogin("guest")
                .setClientPasscode("guest");
*/
        // 하지만 redis의 pub-sub 지원(동기화)은 안해줘서 이건 직접 구현해야됨

        // 클라이언트가 메시지를 구독(수신)할 때 사용할 접두사
        // /sub은 1:N (방, 브로드캐스트), /queue는 1:1 (DM)에 사용
        registry.enableSimpleBroker("/sub", "/queue");

        // 클라이언트가 서버로 메시지를 발행(송신)할 때 사용할 접두사
        // 프론트엔드에서 보낸 목적지 주소가 /pub/... 으로 시작하면 Controller로 라우팅
        registry.setApplicationDestinationPrefixes("/pub");

        // 에러 핸들링을 위한 prefix 설정
        registry.setUserDestinationPrefix("/user");
    }


    /**
     * 그 외 WebSocketMessageBrokerConfigurer의 메서드 기능 간단한 설명
     * 1. registerStompEndpoints: 프론트엔드가 최초로 웹소켓 연결(Handshake)을 맺기 위해 찾아올 엔드포인트 주소(URL)와 CORS(교차 출처) 범위를 설정
     * 2. configureMessageBroker: 서버 내부로 들어온 메시지가 특정 방(/topic)으로 갈지 개인 큐(/queue)로 갈지 목적지(라우팅) 규칙을 설정하고 내장 브로커를 활성화
     * 3. configureClientInboundChannel: 클라이언트가 보내는 모든 메시지를 컨트롤러에 닿기 전에 미리 가로채서, JWT 토큰이 유효한지 검사하거나 악성 유저를 차단하는 등 인증/보안 처리
     * 4. configureWebSocketTransport: 사용자가 사진이나 엄청난 장문의 텍스트를 보낼 때 서버가 끊기지 않도록, 한 번에 전송 가능한 메시지의 최대 용량이나 통신 제한 시간을 넉넉하게 조절
     * 등등 ...
     */
}