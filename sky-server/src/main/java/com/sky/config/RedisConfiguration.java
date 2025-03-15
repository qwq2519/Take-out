package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfiguration {

    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("RedisTemplate开始创建");
        RedisTemplate redisTemplate = new RedisTemplate();

        redisTemplate.setConnectionFactory(redisConnectionFactory);

        //当前配置类不是必须的，但是springboot的默认序列化器是JdkSerializationRedisSerializer，有点差别
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        return redisTemplate;
    }
}
