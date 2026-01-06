package com.jy.shoppy.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@EnableRedisHttpSession
public class SessionConfig {

    /**
     * Redis 세션 Serializer를 JSON으로 변경
     * - CartProductResponse 등 Serializable 없이도 저장 가능
     * - Redis 데이터 가독성 향상
     */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        // ObjectMapper 설정
        ObjectMapper objectMapper = new ObjectMapper();

        // Java 8 날짜/시간 타입 지원 (LocalDateTime 등)
        objectMapper.registerModule(new JavaTimeModule());

        // Spring Security 타입 지원 (SecurityContext 등)
        objectMapper.registerModules(SecurityJackson2Modules.getModules(getClass().getClassLoader()));

        // 타임스탬프 대신 ISO-8601 형식 사용
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 타입 정보 포함 (역직렬화 시 필요)
        BasicPolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();

        objectMapper.activateDefaultTyping(
                typeValidator,
                ObjectMapper.DefaultTyping.NON_FINAL
        );

        // JSON Serializer 반환
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }
}