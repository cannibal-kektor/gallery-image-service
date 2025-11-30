package kektor.innowise.gallery.image.conf;

import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.LoggingCacheErrorHandler;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.StringUtils;

import java.util.Set;

@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
public class CacheConfig implements CachingConfigurer {

    public static final String URLS_CACHE_LOCAL = "urlsCacheLocal";
    public static final String URLS_CACHE_REMOTE = "urlsCacheRemote";
    public static final String USERS_CACHE_LOCAL = "usersCacheLocal";
    public static final String USERS_CACHE_REMOTE = "usersCacheRemote";

    @Override
    public CacheErrorHandler errorHandler() {
        return new LoggingCacheErrorHandler();
    }

    @Bean
    @Primary
    CompositeCacheManager twoLevelCacheManager(CaffeineCacheManager caffeineCacheManager,
                                               RedisCacheManager redisCacheManager) {
        return new CompositeCacheManager(
                caffeineCacheManager,
                redisCacheManager
        );
    }

    @Bean
    RedisCacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory,
                                        CacheProperties cacheProperties) {
        var builder = RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(createConfiguration(cacheProperties));
        builder.initialCacheNames(Set.of(URLS_CACHE_REMOTE, USERS_CACHE_REMOTE));
        return builder.build();
    }

    @Bean
    CaffeineCacheManager caffeineCacheManager(CacheProperties cacheProperties) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        String specification = cacheProperties.getCaffeine().getSpec();
        if (StringUtils.hasText(specification)) {
            cacheManager.setCacheSpecification(specification);
        }
        cacheManager.setCacheNames(Set.of(URLS_CACHE_LOCAL, USERS_CACHE_LOCAL));
        return cacheManager;
    }

    private RedisCacheConfiguration createConfiguration(CacheProperties cacheProperties) {
        CacheProperties.Redis redisProperties = cacheProperties.getRedis();
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()));
        if (redisProperties.getTimeToLive() != null) {
            config = config.entryTtl(redisProperties.getTimeToLive());
        }
        if (!redisProperties.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }
        return config;
    }

}