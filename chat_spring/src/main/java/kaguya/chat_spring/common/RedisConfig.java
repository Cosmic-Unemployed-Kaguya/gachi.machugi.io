package kaguya.chat_spring.common;

import kaguya.chat_spring.websocket.chat.service.RedisSubscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    /**
     * Redis 연결 팩토리 생성 (Lettuce 사용)
     * Jedis 대신 비동기 요청 처리에 강한 Lettuce 클라이언트를 사용하여 연결
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    /**
     * 메시지 리스너 컨테이너 설정 (pub/sub)
     * 동적 채널 수신을 위해 PatternTopic을 사용하여 멀티 토픽을 구독하도록 설정
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // PatternTopic을 사용해 여러 개의 동적 토픽 패턴을 리스너에 등록
        container.addMessageListener(listenerAdapter, List.of(
                new PatternTopic("room:*"),  // room:1, room:2 등 모든 방 메시지 수신
                new PatternTopic("user:*"),  // user:admin, user:user1 등 모든 DM 메시지 수신
                new PatternTopic("broadcast")  // 전역 공지 메시지 수신
        ));

        return container;
    }

    /**
     * 메시지를 수신할 어댑터 설정 (pub/sub)
     * Redis에서 전달된 메시지를 RedisSubscriber의 onMessage() 메서드로 연결해주는 어댑터
     * RedisMessageListenerContainer가 메시지를 수신하면 해당 메서드를 자동으로 호출
     */
    @Bean
    public MessageListenerAdapter listenerAdapter(RedisSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "onMessage");
    }

    /**
     * Redis 캐싱 및 저장용 RedisTemplate 설정
     * 기본 RedisTemplate은 JdkSerializationRedisSerializer를 사용하기 때문에,
     * redis-cli 등에서 데이터를 확인할 때 바이너리 값으로 보여 식별이 어려움
     * 그래서 Key와 Value에 대한 직렬화 방식을 명시적으로 설정
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {

        // ConnectionFactory 연결
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        /**
         * Key-Value, List, Set 직렬화 설정
         * Key 직렬화: StringRedisSerializer (String)
         * Value 직렬화: GenericJackson2JsonRedisSerializer (Json/Object)
         */
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        /**
         * Hash 자료구조 직렬화 설정
         * Key 직렬화: StringRedisSerializer (String)
         * Value 직렬화: GenericJackson2JsonRedisSerializer (Json/Object)
         */
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        return redisTemplate;
    }

    /**
     * Pub/Sub 메시지 발행용 RedisTemplate 설정
     * Key와 Value 모두 순수 문자열(StringRedisSerializer)로 전송
     * pub/sub은 GenericJackson2JsonRedisSerializer를 사용하지 않는게 표준 (다른 프레임워크도 쓸 수 있어야 하기 때문에)
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
