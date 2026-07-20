package kaguya.chat_spring.chat.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import kaguya.chat_spring.chat.model.BroadcastDto;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class BroadcastSubscriber extends BaseSubscriber<BroadcastDto> {

    // 의존성 주입 (@Lazy 필요)
    public BroadcastSubscriber(ObjectMapper objectMapper, @Lazy SimpMessagingTemplate messagingTemplate) {
        super(objectMapper, messagingTemplate, BroadcastDto.class);
    }

    @Override
    protected String getDestination(String topic, BroadcastDto dto) {
        return "/topic/broadcast";
    }
}