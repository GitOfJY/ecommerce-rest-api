package com.jy.shoppy.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("dev")
@AutoConfigureMockMvc
@Slf4j
public class RedisSessionStructureTest {
    @Autowired StringRedisTemplate redisTemplate;
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    private String sessionIdForCleanup;

    @AfterEach
    void cleanup() {
        if (sessionIdForCleanup == null) return;

        redisTemplate.delete("spring:session:sessions:" + sessionIdForCleanup);
        redisTemplate.delete("spring:session:sessions:expires:" + sessionIdForCleanup);
    }

    @Test
    void inspect_session_structure_after_login() throws Exception {
        // given - 로그인 성공 → SESSION 쿠키 확보
        String encodedSessionId = givenLoginAndGetSessionId("test@test.com", "1234");
        String sessionId = new String(Base64.getDecoder().decode(encodedSessionId));
        sessionIdForCleanup = sessionId;

        // when - Redis 세션 키/필드 조회
        String sessionKey = "spring:session:sessions:" + sessionId;

        Boolean keyExists = redisTemplate.hasKey(sessionKey);
        Boolean hasCreationTime = redisTemplate.opsForHash().hasKey(sessionKey, "creationTime");
        Boolean hasSecurityContext = redisTemplate.opsForHash().hasKey(sessionKey, "sessionAttr:SPRING_SECURITY_CONTEXT");

        // then
        assertThat(keyExists).isTrue();
        assertThat(hasCreationTime).isTrue();
        assertThat(hasSecurityContext).isTrue();
    }

    private String givenLoginAndGetSessionId(String email, String password) throws Exception {
        LoginRequest req = new LoginRequest(email, password);

        MockHttpServletResponse response = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        String setCookie = response.getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookie)
                .as("로그인 성공 시 Set-Cookie로 SESSION이 내려와야 함")
                .isNotBlank();

        return extractCookieValue(setCookie, "SESSION")
                .orElseThrow(() -> new IllegalStateException("SESSION 쿠키를 찾지 못했습니다. Set-Cookie=" + setCookie));

    }

    private Optional<String> extractCookieValue(String setCookieHeader, String cookieName) {
        Pattern p = Pattern.compile(cookieName + "=([^;]+)");
        Matcher m = p.matcher(setCookieHeader);
        if (!m.find()) return Optional.empty();
        return Optional.of(m.group(1));
    }

    static class LoginRequest {
        public String email;
        public String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }
}