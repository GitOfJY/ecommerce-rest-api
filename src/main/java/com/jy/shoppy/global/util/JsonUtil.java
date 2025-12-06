package com.jy.shoppy.global.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class JsonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static <T> String toJson(T object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("JSON 직렬화 실패: {}", e.getMessage(), e);
            throw new ServiceException(ServiceExceptionCode.JSON_PROCESSING_ERROR);
        }
    }

    public static <T> T fromJson(String jsonString, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonString, clazz);
        } catch (JsonProcessingException e) {
            throw new ServiceException(ServiceExceptionCode.JSON_PROCESSING_ERROR);
        }
    }

    public static <T> List<T> fromJsonList(String jsonString, Class<T> clazz) {
        try {
            CollectionType type = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, clazz);

            return objectMapper.readValue(jsonString, type);
        } catch (JsonProcessingException e) {
            log.error("JSON 직렬화 실패: {}", e.getMessage(), e);
            throw new ServiceException(ServiceExceptionCode.JSON_PROCESSING_ERROR);
        }
    }
}
