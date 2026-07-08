package kaguya.chat_spring.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
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

    /**
     * RedisTemplate 설정
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
}
