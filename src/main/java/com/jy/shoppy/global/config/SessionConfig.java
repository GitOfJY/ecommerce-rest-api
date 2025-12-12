package com.jy.shoppy.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@EnableRedisHttpSession
public class SessionConfig {
//    @Bean
//    public GenericJackson2JsonRedisSerializer springSessionDefaultRedisSerializer() {
//        ObjectMapper objectMapper = new ObjectMapper();
//
//        // SimpleGrantedAuthority 직렬화 지원
//        objectMapper.registerModules(SecurityJackson2Modules.getModules(getClass().getClassLoader()));
//        objectMapper.activateDefaultTyping(
//                BasicPolymorphicTypeValidator.builder()
//                        .allowIfBaseType(Object.class)
//                        .build(),
//                ObjectMapper.DefaultTyping.NON_FINAL
//        );
//
//        // 세션 속성 값을 Java 직렬화 대신 JSON 형태로 저장하도록 설정
//        return new GenericJackson2JsonRedisSerializer();
//    }

    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new JdkSerializationRedisSerializer();
    }
}
