package ltd.newbee.mall.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置：把 RedisTemplate 的默认序列化方式换成 JSON。
 */
@Configuration
public class RedisConfig {

    /**
     * Spring Boot 自动配置的 RedisTemplate 用的是 JdkSerializationRedisSerializer，
     * 它会把对象序列化成 Java 二进制流，写进 Redis 后是一堆 "\xac\xed" 开头的乱码，
     * 既没法在 redis-cli 里肉眼排查，也依赖类的全限定名、跨语言不可用。
     * <p>
     * 这里改成：key 用字符串序列化，value 用 JSON 序列化，
     * 这样缓存内容在 redis-cli 里直接可读（例如 GET newbee:index:goods:3:4 就能看到商品 JSON）。
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        StringRedisSerializer keySerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer();

        redisTemplate.setKeySerializer(keySerializer);
        redisTemplate.setHashKeySerializer(keySerializer);
        redisTemplate.setValueSerializer(valueSerializer);
        redisTemplate.setHashValueSerializer(valueSerializer);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
