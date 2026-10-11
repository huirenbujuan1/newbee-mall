package ltd.newbee.mall.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 缓存读写工具：把「查缓存 / 写缓存 / 删缓存」三件事收敛到一处，避免每个 service 各写一份。
 */
@Component
public class RedisCacheUtil {

    /**
     * 缓存基础过期时间：30 分钟。
     */
    private static final long BASE_TTL_SECONDS = 30 * 60;

    /**
     * TTL 随机抖动范围（秒）。真正的过期时间 = 30 分钟 ± 60 秒。
     * <p>
     * 目的是防「缓存雪崩」：如果所有 key 的 TTL 完全一样，一旦同时写入，
     * 30 分钟后会在同一秒集体失效，请求瞬间全部打到数据库。
     * 加一个随机偏移把失效时间打散。
     */
    private static final int TTL_JITTER_SECONDS = 60;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 读缓存，未命中返回 null。
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    /**
     * 写缓存。值为 null 时不写（避免把「查不到」的临时结果固化 30 分钟）。
     */
    public void set(String key, Object value) {
        if (value == null) {
            return;
        }
        long ttl = BASE_TTL_SECONDS + ThreadLocalRandom.current().nextLong(-TTL_JITTER_SECONDS, TTL_JITTER_SECONDS + 1);
        redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS);
    }

    /**
     * 删缓存。数据库数据变更后调用，让下一次访问重新回源建缓存。
     * <p>
     * 注意这里删的是**整个 key**，也就是 Cache-Aside 模式里的「更新数据库 + 删除缓存」，
     * 而不是「更新数据库 + 更新缓存」——后者在并发下更容易出现两个请求写回新旧不一致的数据。
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
