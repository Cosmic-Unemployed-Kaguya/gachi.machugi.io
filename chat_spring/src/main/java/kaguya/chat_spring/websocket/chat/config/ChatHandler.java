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
     * 더 필요한 작업
     * * 1. Heartbeat (유령세션 제거)
     * - 마지막으로 통신한 시간을 함께 기록하는 map 하나 더 만들어서 주기적으로(30초 정도) ping 을 보내고 pong을 받도록 해야함
     * - 만약 pong을 받는다면 map에 최신 시간으로 갱신함
     * - 스케쥴러로 주기적으로 map 돌면서 (현재시간 - 마지막 통신 시간)을 계산하고 값이 기준치보다 크다면 세션을 종료
     * - (한계점)
     * - 30초 마다 모든 접속된 유저에게 ping-pong 요청하고 스케쥴러로 검사하는데, 이게 서버의 부하가 큼
     * - 스케줄러가 유저에게 Ping을 쏘려는 찰나에, 누군가 채팅을 쳐서 서버가 동일한 유저에게 메시지를 보낼때의 접근 처리 해야 됨
     * - 페이로드 경량화하고, 최근에 작업이 없었던 유저 골라서 ping 요청, 스레드 동시 접근 방어 하는 등의 방법으로 부하를 최소화해야 함
     *
     * 2. 세션 동시 전송 충돌 방지
     * - 동시 접근 같은 경우에는 세션에 대한 lock 걸고 풀거나 데코레이터로 감싸는 등의 작업을 해야함
     * - 이게 상태 저장소(Map) 동시접근, redis에 접근하기 위한 입장 순서 구현 등의 복잡하고 귀찮은 작업을들 내가 직접 구현해줘야 하고
     * - 다중 쓰레드 환경에서의 데이터 무결성을 보장해주는 코드와 로직 충돌 처리 하는 것도 별개로 구현을 해야됨
     *
     * 3. 보안적인 처리 (인증 및 사칭 방지): onlyCookie 방식 사용해서 이 문제는 발생하진 않지만 bearer토큰을 사용한다는 가정으로 했을 때
     * - 기본 웹소켓 API는 첫 연결(Handshake) 시 HTTP 헤더(Authorization: Bearer 등)를 마음대로 조작해 보낼 수 없음
     * - 때문에 JWT 토큰을 URL 쿼리 파라미터로 넘기거나(로그 노출 위험), 연결 직후 첫 메시지로 토큰을 보내 검증하는 꼼수를 써야 함
     * - 클라이언트가 보내는 JSON 데이터(예: "sender": "admin")를 서버가 무비판적으로 신뢰하게 되어 악의적인 유저의 발신자 사칭이 쉬움
     * - 이를 방어하려면 HandshakeInterceptor를 구현하여 연결 전에 토큰을 가로채 검증하고, 진짜 신분을 세션에 강제 주입하는 로직을 직접 짜야 함
     *
     * 위 작업들 전부다 STOMP가 효율적으로 구현해 놨는데 굳이 내가?
     */
}