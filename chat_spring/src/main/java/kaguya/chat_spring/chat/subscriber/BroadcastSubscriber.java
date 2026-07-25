package kaguya.chat_spring.chat.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import kaguya.chat_spring.chat.model.dto.request.TalkReq;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class BroadcastSubscriber extends BaseSubscriber<TalkReq> {

    // 의존성 주입 (@Lazy 필요)
    public BroadcastSubscriber(ObjectMapper objectMapper, @Lazy SimpMessagingTemplate messagingTemplate) {
        super(objectMapper, messagingTemplate, TalkReq.class);
    }

    @Override
    protected String getDestination(String topic, TalkReq dto) {
        return "/sub/broadcast";
    }
}