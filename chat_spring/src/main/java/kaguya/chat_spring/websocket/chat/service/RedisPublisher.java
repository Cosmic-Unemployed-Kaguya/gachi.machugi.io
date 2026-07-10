package kaguya.chat_spring.websocket.chat.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisPublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    // 생성자 (@Qualifier 명시)
    public RedisPublisher(@Qualifier("pubSubRedisTemplate") RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 메세지 발행
    public void publish(String topic, String message) {
        redisTemplate.convertAndSend(topic, message);
    }
}