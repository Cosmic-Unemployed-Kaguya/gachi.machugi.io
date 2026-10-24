package kaguya.chat_spring.backup.raw_websocket.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kaguya.chat_spring.chat.common.RedisPublisher;
import kaguya.chat_spring.backup.raw_websocket.common.ChatPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

//@Service
@RequiredArgsConstructor
public class WebSocketChatService {

    private final ObjectMapper objectMapper;
    private final RedisPublisher redisPublisher;

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

        // 방에 없는 사람이 채팅치는 것을 방지
        if (payload.type() == ChatPayload.MessageType.TALK) {
            String currentRoomId = sessionRoomMap.get(session.getId());

            // 사용자가 어떤 방에도 속해있지 않거나, 속해있는 방과 요청한 방(roomId)이 다를 경우
            if (currentRoomId == null || !currentRoomId.equals(payload.roomId())) {
                // 경고 메시지 전송
                ChatPayload errorNotice = new ChatPayload(
                        ChatPayload.MessageType.TALK,
                        payload.roomId(),
                        "SYSTEM",
                        payload.sender(),
                        "잘못된 접근입니다. 해당 방에 입장하지 않았습니다."
                );
                // 현재 세션에만 에러 메시지 전송 후 로직 종료
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorNotice)));
                return;
            }
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


    /**
     * 사용자가 많아져서 채팅 서버를 2대(Server A, Server B)로 늘렸을 때 다른 서버에 있는 사용자에게 보내기 위해
     * Redis Pub/Sub을 이용해서 브로커의 기능(데이터 동기화)을 직접 구현함
     * 보통은 이 지점에서 직접 구현하기가 까다로워져서 외부 브로커 연동이 쉬운 WebSocket Broker(STOMP) 방식을 많이 사용함
     */

    // ===================================
    // 세부 비지니스 로직: publisher 설정
    // ===================================

    // 방 입장
    private void handleEnterRoom(WebSocketSession session, ChatPayload payload) throws IOException {
        // 방 ID 가져오기
        String roomId = payload.roomId();

        // 방이 없으면 생성하고, 로컬 스토리지에 세션을 추가
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
        // Redis로 발행하기 위한 json을 String 타입으로 변경
        String message = objectMapper.writeValueAsString(systemNotice);

        // 메시지 발행 (publish)
        redisPublisher.publish("room:" + roomId, message);
    }

    // 방 대화
    private void handleRoomTalk(ChatPayload payload) throws IOException {
        String roomId = payload.roomId();

        String message = objectMapper.writeValueAsString(payload);

        redisPublisher.publish("room:" + roomId, message);
    }

    // 전체 공지
    private void handleBroadcast(ChatPayload payload) throws IOException {
        // 기존 로직은 유저 목록을 하나씩 까서 전부 로컬 스토리지에 저장하는 로직이었음
        // 그냥 메시지에 대해서 한번 publish 하면 알아서 subscriber가 가져가기 때문에 redis의 pub-sub 적용하면서 그렇게 할 필요 없어짐

        String message = objectMapper.writeValueAsString(payload);

        redisPublisher.publish("broadcast", message);
    }

    // 귓속말 (DM)
    private void handleDM(ChatPayload payload) throws IOException {
        // 기존 로직은 해당 유저가 있는지 로컬 스토리를 전부 순회하면서 찾아보고
        // 있으면(접속중이면) 그 사람에게 메시지 전송하고 없으면(접속중이 아니라면) 없다는 메시지를 회신하는 로직이었음
        // 위와 마찬가지로 pub-sub 도입하면서 일련의 작업 필요없어지고, 그냥 userId로 publish 하면 끝
        // 유저가 없을 때 메시지 회신하는건 redis에 online 이라는 공간을 만들어서 거기에 있는지 확인하는 로직을 짜야함(아직 안함)

        // 수신자
        String receiverName = payload.receiver();
        String message = objectMapper.writeValueAsString(payload);

        redisPublisher.publish("user:" + receiverName, message);
    }


    // ============================================================
    // 세부 비지니스 로직: Subscriber 설정 (RedisSubscriber가 호출)
    // ============================================================

    // 특정 방의 로컬 유저들에게 메시지 전송
    public void sendToLocalRoom(String roomId, ChatPayload payload) throws IOException {
        // 로컬 스토리지에서 방 세션 가져오기
        Set<WebSocketSession> roomSessions = roomSessionMap.get(roomId);

        if (roomSessions != null) {
            // payload에서 message 가져오기
            String message = objectMapper.writeValueAsString(payload);
            TextMessage textMessage = new TextMessage(message);

            // room 세션 전부 돌면서 방 안에 있는 사람에게 전부 전송
            for (WebSocketSession session : roomSessions) {
                // 세션에 연결되어있는 사람만 전송
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }
        }
    }

    // 전체 로컬 유저들에게 메시지 전송
    public void sendToAllLocalUsers(ChatPayload payload) throws IOException {

        String jsonMessage = objectMapper.writeValueAsString(payload);
        TextMessage textMessage = new TextMessage(jsonMessage);

        for (WebSocketSession session : userSessionMap.values()) {
            if (session.isOpen()) {
                session.sendMessage(textMessage);
            }
        }
    }

    // 특정 로컬 유저 1명에게 DM 전송
    public void sendToLocalUser(String receiverName, ChatPayload payload) throws IOException {

        WebSocketSession targetSession = userSessionMap.get(receiverName);

        if (targetSession != null && targetSession.isOpen()) {
            String jsonMessage = objectMapper.writeValueAsString(payload);
            targetSession.sendMessage(new TextMessage(jsonMessage));
        }
    }
}
