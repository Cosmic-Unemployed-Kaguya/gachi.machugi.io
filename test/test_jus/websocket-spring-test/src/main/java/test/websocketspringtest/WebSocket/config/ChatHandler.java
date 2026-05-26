package test.websocketspringtest.WebSocket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import test.websocketspringtest.WebSocket.model.ChatPayload;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 방(Room) 관리를 위한 컬렉션
    // [방 ID : 해당 방에 참여 중인 세션들]
    private final Map<String, Set<WebSocketSession>> roomSessionMap = new ConcurrentHashMap<>();
    // [세션 ID : 해당 세션이 위치한 방 ID] - 연결 종료 시 빠른 정리를 위함
    private final Map<String, String> sessionRoomMap = new ConcurrentHashMap<>();

    // 사용자(User) 관리를 위한 컬렉션 - DM 발송 시 대상 검색용
    // [사용자 닉네임/ID : 해당 사용자의 WebSocketSession]
    private final Map<String, WebSocketSession> userSessionMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("웹 소켓 세션 연결됨: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 수신된 JSON 문자열을 ChatPayload 레코드로 변환
        ChatPayload payload = objectMapper.readValue(message.getPayload(), ChatPayload.class);

        // 발신자 정보가 있다면 DM 처리를 위해 세션 맵에 등록 및 갱신
        if (payload.sender() != null && !payload.sender().trim().isEmpty()) {
            userSessionMap.put(payload.sender(), session);
        }

        // 메시지 타입에 따른 분기 처리
        switch (payload.type()) {
            case ENTER:
                handleEnterRoom(session, payload);
                break;
            case TALK:
                handleRoomTalk(payload);
                break;
            case BROADCAST:
                handleBroadcast(payload);
                break;
            case DM:
                handleDM(payload);
                break;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();

        // 방 목록에서 세션 제거
        String roomId = sessionRoomMap.get(sessionId);
        if (roomId != null) {
            Set<WebSocketSession> roomSessions = roomSessionMap.get(roomId);
            if (roomSessions != null) {
                roomSessions.remove(session);
                // 방에 남은 인원이 없으면 방 자체를 삭제
                if (roomSessions.isEmpty()) {
                    roomSessionMap.remove(roomId);
                }
            }
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
        String roomId = payload.roomId();

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
        String jsonMessage = objectMapper.writeValueAsString(payload);
        TextMessage textMessage = new TextMessage(jsonMessage);

        // 접속 중인 모든 사용자(userSessionMap에 등록된 세션)에게 메시지 발송
        for (WebSocketSession s : userSessionMap.values()) {
            if (s.isOpen()) {
                s.sendMessage(textMessage);
            }
        }
    }

    // DM
    private void handleDM(ChatPayload payload) throws IOException {
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
        Set<WebSocketSession> roomSessions = roomSessionMap.get(roomId);
        if (roomSessions != null) {
            String jsonMessage = objectMapper.writeValueAsString(payload);
            TextMessage textMessage = new TextMessage(jsonMessage);

            for (WebSocketSession session : roomSessions) {
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