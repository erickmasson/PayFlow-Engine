package com.payflow_engine.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    @SuppressWarnings("deprecation") // <--- Oculta o aviso amarelo na sua IDE
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // As chaves serão Strings
        template.setKeySerializer(new StringRedisSerializer());

        // Os valores serão convertidos para JSON usando o Jackson 2 (Padrão do Spring Boot 3)
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }
}