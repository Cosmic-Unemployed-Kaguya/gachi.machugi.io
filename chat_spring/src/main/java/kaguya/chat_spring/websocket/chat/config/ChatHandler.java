package kaguya.chat_spring.websocket.chat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import kaguya.chat_spring.websocket.chat.service.WebSocketChatService;
import kaguya.chat_spring.websocket.chat.model.ChatPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 정통 WebSocket 방식은
 * - Controller 개념이 없음. WebSocketHandler 클래스가 모든 라우팅 및 비즈니스 로직을 담당함
 * - 접속한 사용자의 WebSocketSession을 서버 메모리의 List나 Map에 직접 저장하고, 메시지를 보낼 때마다 반복문을 돌며 발송해야 함
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final WebSocketChatService chatService;

    // 클라이언트가 웹소켓 연결을 성공하면 스프링이 WebSocketSession 객체를 생성 (Callback)
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("웹 소켓 세션 연결됨: {}", session.getId());

        // todo. 만약 로직을 추가한다면
        // 1. 인증 및 인가 확인
        // 2. 접속자 수 카운팅
        // 3. 전역 세션 저장
    }

    /**
     * 클라이언트가 서버로 텍스트 메시지를 보냈을 때 type 확인하고 어떤 작업을 할건지 핸들링
     * @param session: 현재 사용자의 세션
     * @param message: 클라이언트가 서버로 보낸 데이터
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 수신된 JSON 문자열을 Object(ChatPayload)로 변환
        ChatPayload payload = objectMapper.readValue(message.getPayload(), ChatPayload.class);

        chatService.processMessage(session, payload);
    }

    /**
     * 웹소켓 연결이 끊겼을 때
     * @param session: 현재 사용자의 세션
     * @param status: 연결이 끊어진 이유와 상태 코드
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 세션 ID 가져오기
        String sessionId = session.getId();

        chatService.deleteSession(session, sessionId);
        
        log.info("웹 소켓 세션 종료됨: {}", sessionId);
    }

    /**
     * 현재 로직은 ConcurrentHashMap이라는 서버의 로컬 메모리에 데이터를 관리하고 있음
     * 사용자가 많아져서 채팅 서버를 2대(Server A, Server B)로 늘렸을 때 다른 서버에 있는 사용자에게 보낼 방법이 없음
     * 그래서, Redis Pub/Sub이나 RabbitMQ, Kafka 등을 도입하고 브로커 역할을 해주는 로직을 짜야함
     * 이 지점에서 직접 구현하기가 까다로워져서 외부 브로커 연동이 쉬운 WebSocket Broker(STOMP) 방식을 많이 사용함
     */
}