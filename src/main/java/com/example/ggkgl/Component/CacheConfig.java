package com.example.ggkgl.Component;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Collections;

/**
 * 缓存过期时间配置
 **/
@Configuration
@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {
    @Resource
    private RedisConnectionFactory connectionFactory;

    @Bean
    public CacheManager cacheManager(RedisTemplate redisTemplate) {
        /* 默认配置*/
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig().disableCachingNullValues();

        /* 配置tableConfigService的超时时间为30min*/
        return RedisCacheManager.builder(RedisCacheWriter.lockingRedisCacheWriter
                (connectionFactory)).cacheDefaults(defaultCacheConfig).withInitialCacheConfigurations(Collections.singletonMap
                ("test", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(30L))
                        .disableCachingNullValues())).transactionAware().build();
    }
}