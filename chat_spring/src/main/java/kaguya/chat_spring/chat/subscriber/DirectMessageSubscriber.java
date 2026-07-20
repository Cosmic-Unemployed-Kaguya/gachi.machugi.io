package kaguya.chat_spring.chat.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import kaguya.chat_spring.chat.model.DirectMessageDto;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class DirectMessageSubscriber extends BaseSubscriber<DirectMessageDto> {

    // 의존성 주입 (@Lazy 필요)
    public DirectMessageSubscriber(ObjectMapper objectMapper, @Lazy SimpMessagingTemplate messagingTemplate) {
        super(objectMapper, messagingTemplate, DirectMessageDto.class);
    }

    @Override
    protected String getDestination(String topic, DirectMessageDto dto) {
        return "/queue/dm/" + dto.receiver();
    }
}