package com.jy.shoppy.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@EnableRedisHttpSession
public class SessionConfig {
    @Bean
    public GenericJackson2JsonRedisSerializer springSessionDefaultRedisSerializer() {
        // 세션 속성 값을 Java 직렬화 대신 JSON 형태로 저장하도록 설정
        return new GenericJackson2JsonRedisSerializer();
    }
}
