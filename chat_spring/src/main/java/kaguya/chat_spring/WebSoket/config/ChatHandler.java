package kaguya.chat_spring.WebSoket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import kaguya.chat_spring.WebSoket.model.ChatPayload;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 정통 WebSocket 방식은
 * - Controller 개념이 없음. WebSocketHandler 클래스가 모든 라우팅 및 비즈니스 로직을 담당함
 * - 접속한 사용자의 WebSocketSession을 서버 메모리의 List나 Map에 직접 저장하고, 메시지를 보낼 때마다 반복문을 돌며 발송해야 함
 */
@Component
public class ChatHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // [방 ID : 해당 방에 참여 중인 세션들] - 방(Room)의 세션 관리를 위함
    private final Map<String, Set<WebSocketSession>> roomSessionMap = new ConcurrentHashMap<>();
    // [세션 ID : 해당 세션이 위치한 방 ID] - 유저가 어디 방에 있는지? (연결 종료 시 빠른 정리를 위함)
    private final Map<String, String> sessionRoomMap = new ConcurrentHashMap<>();
    // [사용자 닉네임/ID : 해당 사용자의 WebSocketSession] - 사용자 관리를 위한 컬렉션 (DM 발송 시 대상 검색용)
    private final Map<String, WebSocketSession> userSessionMap = new ConcurrentHashMap<>();

    // 클라이언트가 웹소켓 연결을 성공하면 스프링이 WebSocketSession 객체를 생성 (Callback)
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("웹 소켓 세션 연결됨: " + session.getId());

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

        // 발신자 정보가 있다면 DM 처리를 위해 세션 맵에 등록 및 갱신
        if (payload.sender() != null && !payload.sender().trim().isEmpty()) {
            userSessionMap.put(payload.sender(), session);
        }

        // 메시지 타입에 따른 분기 처리
        switch (payload.type()) {
            case ENTER:  // 방 입장
                handleEnterRoom(session, payload);
                break;
            case TALK:  // 메시지 전송
                handleRoomTalk(payload);
                break;
            case BROADCAST:  // 공지
                handleBroadcast(payload);
                break;
            case DM:  // 귓속말(DM)
                handleDM(payload);
                break;
        }
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

        // 연결된 모든 관리용 세션 삭제
        String roomId = sessionRoomMap.get(sessionId);
        if (roomId != null) {
            // 방에 있는 유저(세션)들 가져오기
            Set<WebSocketSession> roomSessions = roomSessionMap.get(roomId);
            if (roomSessions != null) {
                // 나간 유저의 세션 삭제
                roomSessions.remove(session);
                // 방에 남은 인원이 없으면 방 자체를 삭제
                if (roomSessions.isEmpty()) {
                    roomSessionMap.remove(roomId);
                }
            }
            // 유저가 방에 나감
            sessionRoomMap.remove(sessionId);
            System.out.println("세션 " + sessionId + "가 [" + roomId + "] 방에서 퇴장했습니다.");
        }

        // 전체 사용자 세션 맵에서 제거
        userSessionMap.values().remove(session);
        System.out.println("웹 소켓 세션 종료됨: " + sessionId);
    }


    // ==============================
    // 세부 비즈니스 로직 처리 메서드
    // ==============================

    // 방 입장
    private void handleEnterRoom(WebSocketSession session, ChatPayload payload) throws IOException {
        // 방 ID 가져오기
        String roomId = payload.roomId();

/*
        // 방이 없으면 생성하고, 세션을 추가
        Set<WebSocketSession> sessions = roomSessionMap.get(roomId);
        if (sessions == null) {
            sessions = ConcurrentHashMap.newKeySet();  // 새로운 빈 방(Set)을 생성
            roomSessionMap.put(roomId, sessions);  // 맵에 새 방을 등록
        }
        sessions.add(session);
*/
        // 방이 없으면 생성하고, 세션을 추가
        roomSessionMap.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session);
        sessionRoomMap.put(session.getId(), roomId);

        // record는 불변 객체이므로, 알림용 객체를 새로 생성하여 전송
        ChatPayload systemNotice = new ChatPayload(
                ChatPayload.MessageType.ENTER,
                roomId,
                "SYSTEM",
                null,
                payload.sender() + "님이 입장하셨습니다."
        );

        sendToRoom(roomId, systemNotice);
    }

    // 방 대화
    private void handleRoomTalk(ChatPayload payload) throws IOException {
        sendToRoom(payload.roomId(), payload);
    }

    // 전체 공지
    private void handleBroadcast(ChatPayload payload) throws IOException {
        // json -> TextMessage로 변환
        String jsonMessage = objectMapper.writeValueAsString(payload);
        TextMessage textMessage = new TextMessage(jsonMessage);

        // 접속 중인 모든 사용자(userSessionMap에 등록된 세션)에게 메시지 발송
        for (WebSocketSession s : userSessionMap.values()) {
            // session이 연결이 되어있으면 메시지 전송
            if (s.isOpen()) {
                s.sendMessage(textMessage);
            }
        }
    }

    // 귓속말 (DM)
    private void handleDM(ChatPayload payload) throws IOException {
        // 수신자
        String receiverName = payload.receiver();
        WebSocketSession targetSession = userSessionMap.get(receiverName);

        if (targetSession != null && targetSession.isOpen()) {
            // 수신자가 접속 중이면 메시지 전송
            String jsonMessage = objectMapper.writeValueAsString(payload);
            targetSession.sendMessage(new TextMessage(jsonMessage));
        } else {
            // 수신자가 접속 중이 아닐 경우, 보낸 사람에게 시스템 에러 메시지 회신
            WebSocketSession senderSession = userSessionMap.get(payload.sender());
            if (senderSession != null && senderSession.isOpen()) {
                ChatPayload errorNotice = new ChatPayload(
                        ChatPayload.MessageType.DM,
                        null,
                        "SYSTEM",
                        payload.sender(),
                        "수신자 [" + receiverName + "] 님이 오프라인 상태입니다."
                );
                senderSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorNotice)));
            }
        }
    }

    // 특정 방(RoomId)에 참여 중인 모든 세션에게 메시지를 전송하는 공통 유틸 메서드
    private void sendToRoom(String roomId, ChatPayload payload) throws IOException {
        // 방의 접속자(세션) 리스트 가져옴
        Set<WebSocketSession> roomSessions = roomSessionMap.get(roomId);

        if (roomSessions != null) {
            // 방에 사용자 있으면 message 세팅
            String jsonMessage = objectMapper.writeValueAsString(payload);
            TextMessage textMessage = new TextMessage(jsonMessage);

            // 방에 들어와 있는 모든 session에 message 보냄
            for (WebSocketSession session : roomSessions) {
                // 접속했는지 확인
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }
        }
    }

    /**
     * 현재 로직은 ConcurrentHashMap이라는 서버의 로컬 메모리에 데이터를 관리하고 있음
     * 사용자가 많아져서 채팅 서버를 2대(Server A, Server B)로 늘렸을 때 다른 서버에 있는 사용자에게 보낼 방법이 없음
     * 그래서, Redis Pub/Sub이나 RabbitMQ, Kafka 등을 도입하고 브로커 역할을 해주는 로직을 짜야함
     * 이 지점에서 직접 구현하기가 까다로워져서 외부 브로커 연동이 쉬운 WebSocket Broker(STOMP) 방식을 많이 사용함
     */
}