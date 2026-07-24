package kaguya.chat_spring.chat.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import kaguya.chat_spring.chat.model.dto.LobbyDto;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class LobbySubscriber extends BaseSubscriber<LobbyDto> {

    // 의존성 주입 (@Lazy 필요)
    public LobbySubscriber(ObjectMapper objectMapper, @Lazy SimpMessagingTemplate messagingTemplate) {
        super(objectMapper, messagingTemplate, LobbyDto.class);
    }

    @Override
    protected String getDestination(String topic, LobbyDto dto) {
        return "/sub/lobby";
    }
}