package kaguya.chat_spring.chat.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import kaguya.chat_spring.chat.model.dto.RoomDto;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class RoomSubscriber extends BaseSubscriber<RoomDto> {

    // 의존성 주입 (@Lazy 필요)
    public RoomSubscriber(ObjectMapper objectMapper, @Lazy SimpMessagingTemplate messagingTemplate) {
        super(objectMapper, messagingTemplate, RoomDto.class);
    }

    @Override
    protected String getDestination(String topic, RoomDto dto) {
        return "/sub/room/" + dto.roomId();
    }
}