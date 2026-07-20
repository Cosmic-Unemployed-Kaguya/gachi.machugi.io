package kaguya.chat_spring.chat.common;

import kaguya.chat_spring.chat.subscriber.BaseSubscriber;
import kaguya.chat_spring.chat.subscriber.BroadcastSubscriber;
import kaguya.chat_spring.chat.subscriber.DirectMessageSubscriber;
import kaguya.chat_spring.chat.subscriber.LobbySubscriber;
import org.springframework.beans.factory.annotation.Qualifier;
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

    private MessageListenerAdapter createAdapter(BaseSubscriber<?> subscriber) {
        MessageListenerAdapter adapter = new MessageListenerAdapter(subscriber, "onMessage");
        adapter.setSerializer(new StringRedisSerializer());
        return adapter;
    }

    @Bean
    public MessageListenerAdapter roomListenerAdapter(LobbySubscriber subscriber) {
        return createAdapter(subscriber);
    }

    @Bean
    public MessageListenerAdapter lobbyListenerAdapter(LobbySubscriber subscriber) {
        return createAdapter(subscriber);
    }

    @Bean
    public MessageListenerAdapter broadcastListenerAdapter(BroadcastSubscriber subscriber) {
        return createAdapter(subscriber);
    }

    @Bean
    public MessageListenerAdapter dmListenerAdapter(DirectMessageSubscriber subscriber) {
        return createAdapter(subscriber);
    }

    /**
     * 메시지 리스너 컨테이너 설정 (pub/sub)
     * 동적 채널 수신을 위해 PatternTopic을 사용하여 멀티 토픽을 구독하도록 설정
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            @Qualifier("roomListenerAdapter") MessageListenerAdapter roomAdapter,
            @Qualifier("lobbyListenerAdapter") MessageListenerAdapter lobbyAdapter,
            @Qualifier("broadcastListenerAdapter") MessageListenerAdapter broadcastAdapter,
            @Qualifier("dmListenerAdapter") MessageListenerAdapter dmAdapter
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // 컨트롤러에서 설정한 발행(Publish) 토픽 경로
        container.addMessageListener(roomAdapter, new PatternTopic("chat:room:*"));
        container.addMessageListener(lobbyAdapter, new PatternTopic("chat:lobby"));
        container.addMessageListener(broadcastAdapter, new PatternTopic("chat:broadcast"));
        container.addMessageListener(dmAdapter, new PatternTopic("chat:dm:*"));

        return container;
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
