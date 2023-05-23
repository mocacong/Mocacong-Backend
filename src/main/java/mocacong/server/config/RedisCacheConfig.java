package mocacong.server.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@EnableCaching
@Configuration
public class RedisCacheConfig {

    private static final long DELTA_TO_AVOID_CONCURRENCY_TIME = 30 * 60 * 1000L;

    @Value("${security.jwt.token.expire-length}")
    private long accessTokenValidityInMilliseconds;

    @Bean
    @Primary
    public CacheManager cafeCacheManager(RedisConnectionFactory redisConnectionFactory) {
        /*
         * 카페 관련 캐시는 충분히 많이 쌓일 수 있으므로 OOM 방지 차 ttl 12시간으로 설정
         */
        RedisCacheConfiguration redisCacheConfiguration = generateCacheConfiguration()
                .entryTtl(Duration.ofHours(12L));

        return RedisCacheManager.RedisCacheManagerBuilder
                .fromConnectionFactory(redisConnectionFactory)
                .cacheDefaults(redisCacheConfiguration)
                .build();
    }

    @Bean
    public CacheManager oauthPublicKeyCacheManager(RedisConnectionFactory redisConnectionFactory) {
        /*
         * public key 갱신은 1년에 몇 번 안되므로 ttl 3일로 설정
         * 유저가 하루 1번 로그인한다고 가정, 최소 1일은 넘기는 것이 좋다고 판단
         */
        RedisCacheConfiguration redisCacheConfiguration = generateCacheConfiguration()
                .entryTtl(Duration.ofDays(3L));
        return RedisCacheManager.RedisCacheManagerBuilder
                .fromConnectionFactory(redisConnectionFactory)
                .cacheDefaults(redisCacheConfiguration)
                .build();
    }

    @Bean
    public CacheManager accessTokenCacheManager(RedisConnectionFactory redisConnectionFactory) {
        /*
         * accessToken 시간만큼 ttl 설정하되,
         * 만료 직전 캐시 조회하여 로그인 안되는 동시성 이슈 방지를 위해 accessToken ttl 보다 30분 일찍 만료
         */
        RedisCacheConfiguration redisCacheConfiguration = generateCacheConfiguration()
                .entryTtl(Duration.ofMillis(accessTokenValidityInMilliseconds - DELTA_TO_AVOID_CONCURRENCY_TIME));

        return RedisCacheManager.RedisCacheManagerBuilder
                .fromConnectionFactory(redisConnectionFactory)
                .cacheDefaults(redisCacheConfiguration)
                .build();
    }

    private RedisCacheConfiguration generateCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()));
    }
}
