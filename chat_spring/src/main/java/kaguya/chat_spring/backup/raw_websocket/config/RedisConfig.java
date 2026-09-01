package kaguya.chat_spring.backup.raw_websocket.config;

import kaguya.chat_spring.backup.raw_websocket.service.WebSocketSubscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

//@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    /**
     * Redis 연결 팩토리 생성 (Lettuce 사용)
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    /**
     * Raw WebSocket용 어댑터
     * Redis에서 전달된 메시지를 WebSocketSubscriber의 onMessage() 메서드로 연결
     */
    @Bean
    public MessageListenerAdapter rawListenerAdapter(WebSocketSubscriber subscriber) {
        MessageListenerAdapter adapter = new MessageListenerAdapter(subscriber, "onMessage");
        // 에러 증발 방지 및 직렬화 설정
        adapter.setSerializer(new StringRedisSerializer());
        return adapter;
    }

    /**
     * 메시지 리스너 컨테이너 설정 (pub/sub)
     * 동적 채널 수신을 위해 PatternTopic을 사용하여 멀티 토픽 구독
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter rawListenerAdapter
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // PatternTopic을 사용해 여러 개의 동적 토픽 패턴을 리스너에 등록
        List<PatternTopic> topics = List.of(
                new PatternTopic("room:*"),
                new PatternTopic("user:*"),
                new PatternTopic("broadcast")
        );

        // Raw WebSocket 리스너만 단독 등록
        container.addMessageListener(rawListenerAdapter, topics);

        return container;
    }

    /**
     * Redis 캐싱 및 저장용 RedisTemplate 설정
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        // Key-Value 직렬화 설정
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        // Hash 자료구조 직렬화 설정
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        return redisTemplate;
    }

    /**
     * Pub/Sub 메시지 발행용 RedisTemplate 설정
     */
    @Bean(name = "pubSubRedisTemplate")
    public RedisTemplate<String, Object> pubSubRedisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        return redisTemplate;
    }
}