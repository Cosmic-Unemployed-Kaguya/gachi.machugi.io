package kaguya.user.domain.common.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Key : Value 저장
     */
    public void save(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public boolean exist(String key) {
        return redisTemplate.hasKey(key);
    }


    /**
     * 원자적 증감(INCR) / 차감(DECR)
     */
    public Long increment(String key, long timeout, TimeUnit unit) {
        Long count = redisTemplate.opsForValue().increment(key);

        // TTL 설정 (최초 실행시)
        if (count != null && count == 1) {
            redisTemplate.expire(key, timeout, unit);
        }

        return count;
    }
}
