package kaguya.chat_spring.chat.config.handshake;

import kaguya.chat_spring.chat.common.RedisPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompChannelInterceptor implements ChannelInterceptor {

    private final RedisPublisher redisPublisher;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        String destination = accessor.getDestination();
        if (StompCommand.SEND.equals(accessor.getCommand()) && destination != null) {

            // 에러가 넘어오면("/queue/errors") 내장 브로커 처리 (Redis 사용 X)
            if (destination.contains("/queue/errors")) {
                return message;
            }

            // 다중 서버 동기화가 필요한 목적지(/sub, /queue/dm)만 Redis로 Publish
            if (destination.startsWith("/sub")) {
                // byte[] 형태의 payload를 역직렬화 또는 그대로 Redis로 전송하는 로직
                String payload = new String((byte[]) message.getPayload());
                redisPublisher.publish(destination, payload);
            }
        }

        return message;
    }
}
