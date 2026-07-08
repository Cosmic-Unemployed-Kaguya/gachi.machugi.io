package kaguya.chat_spring.websocket.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kaguya.chat_spring.websocket.chat.model.ChatPayload;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketChatService {

    private final ObjectMapper objectMapper;
    // 생성자
    public WebSocketChatService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // [방 ID : 해당 방에 참여 중인 세션들] - 방(Room)의 세션 관리를 위함
    private final Map<String, Set<WebSocketSession>> roomSessionMap = new ConcurrentHashMap<>();
    // [세션 ID : 해당 세션이 위치한 방 ID] - 유저가 어디 방에 있는지? (연결 종료 시 빠른 정리를 위함)
    private final Map<String, String> sessionRoomMap = new ConcurrentHashMap<>();
    // [사용자 닉네임/ID : 해당 사용자의 WebSocketSession] - 사용자 관리를 위한 컬렉션 (DM 발송 시 대상 검색용)
    private final Map<String, WebSocketSession> userSessionMap = new ConcurrentHashMap<>();

    // 메시지 처리
    public void processMessage(WebSocketSession session, ChatPayload payload) throws Exception {

        // 발신자 정보가 있다면 DM 처리를 위해 세션 맵에 등록 및 갱신
        if (payload.sender() != null && !payload.sender().trim().isEmpty()) {
            userSessionMap.put(payload.sender(), session);
        }

        // 비즈니스 로직 분기
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

    // 세션 삭제
    public void deleteSession(WebSocketSession session, String sessionId) {
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
}
