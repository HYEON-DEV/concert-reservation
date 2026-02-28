package kr.hhplus.be.server.config;

import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer()
                )
            )
            .entryTtl(Duration.ofMinutes(3));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaults)
            .withCacheConfiguration(
                "concert:performances",
                defaults.entryTtl(Duration.ofMinutes(10))
            )
            .withCacheConfiguration(
                "concert:seats",
                defaults.entryTtl(Duration.ofSeconds(30))
            )
            .build();
    }
}
