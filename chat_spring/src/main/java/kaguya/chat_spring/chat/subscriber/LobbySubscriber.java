package kaguya.chat_spring.chat.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import kaguya.chat_spring.chat.model.dto.MessageDto;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class LobbySubscriber extends BaseSubscriber<MessageDto> {

    // 의존성 주입 (@Lazy 필요)
    public LobbySubscriber(ObjectMapper objectMapper, @Lazy SimpMessagingTemplate messagingTemplate) {
        super(objectMapper, messagingTemplate, MessageDto.class);
    }

    @Override
    protected String getDestination(String topic, MessageDto dto) {
        return "/sub/lobby";
    }
}