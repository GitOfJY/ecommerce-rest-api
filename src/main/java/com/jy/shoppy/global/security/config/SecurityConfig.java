package com.jy.shoppy.global.security.config;

import com.jy.shoppy.global.security.dsl.RestApiDsl;
import com.jy.shoppy.global.security.entrypoint.RestAuthenticationEntryPoint;
import com.jy.shoppy.global.security.handler.RestAccessDeniedHandler;
import com.jy.shoppy.global.security.handler.RestAuthenticationFailureHandler;
import com.jy.shoppy.global.security.handler.RestAuthenticationSuccessHandler;
import com.jy.shoppy.global.security.provider.RestAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    public static final String[] SECURITY_EXCLUDE_PATHS = {
            "/public/**", "/api/swagger-ui/**", "/swagger-ui/**", "/swagger-ui.html",
            "/api/v3/api-docs/**", "/v3/api-docs/**", "/favicon.ico", "/actuator/**",
            "/swagger-resources/**", "/external/**", "/api/auth/**"
    };

    private final RestAuthenticationProvider restAuthenticationProvider;
    private final RestAuthenticationSuccessHandler restSuccessHandler;
    private final RestAuthenticationFailureHandler restFailureHandler;

    @Bean
    public SecurityFilterChain restSecurityFilterChain(HttpSecurity http) throws Exception {

        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.authenticationProvider(restAuthenticationProvider);
        AuthenticationManager authenticationManager = builder.build();

        http
                .securityMatcher("/api/**")
                .authorizeHttpRequests(auth -> auth
                        // ===== Public 경로 =====
                        .requestMatchers(SECURITY_EXCLUDE_PATHS).permitAll()

                        // ===== 인증 API =====
                        .requestMatchers("/api/auth/**").permitAll()

                        // ===== 상품 조회 - 누구나 =====
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers("/api/category/**").permitAll()

                        // ===== 리뷰 조회 - 누구나 =====
                        .requestMatchers(HttpMethod.GET, "/api/reviews/product/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/*/comments").permitAll()

                        // ===== Redis 재적재 (개발용) =====
                        .requestMatchers(HttpMethod.POST, "/api/products/redis/reload").permitAll()

                        // ===== 주문 - 비회원 주문 허용 =====
                        .requestMatchers(HttpMethod.POST, "/api/orders/guest").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/orders/guest").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/orders/guest/cancel").permitAll()

                        // ===== 쿠폰 - 코드 등록은 인증 필요
                        .requestMatchers(HttpMethod.GET, "/api/coupons/code/**").permitAll()  // 쿠폰 코드 조회
                        .requestMatchers(HttpMethod.POST, "/api/coupons/**").authenticated()  // 쿠폰 등록
                        .requestMatchers(HttpMethod.GET, "/api/coupons/**").authenticated()   // 내 쿠폰 조회

                        // ===== 이메일/비밀번호 찾기 =====
                        .requestMatchers(HttpMethod.POST, "/api/users/find-email").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/reset-password").permitAll()

                        // ===== 관리자 API - ADMIN 권한 필요 =====
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // ===== 이미지 업로드 - 관리자만 =====
                        .requestMatchers("/api/upload/**").hasRole("ADMIN")

                        // ===== 주문 - 인증 필요 =====
                        .requestMatchers("/api/orders/**").authenticated()

                        // ===== 리뷰 작성/수정/삭제 - 인증 필요 =====
                        .requestMatchers("/api/reviews/**").authenticated()

                        // ===== 나머지 모두 인증 필요 =====
                        .anyRequest().authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .authenticationManager(authenticationManager)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new RestAuthenticationEntryPoint())
                        .accessDeniedHandler(new RestAccessDeniedHandler())
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )
                .with(new RestApiDsl<>(), dsl -> dsl
                        .restSuccessHandler(restSuccessHandler)
                        .restFailureHandler(restFailureHandler)
                        .loginProcessingUrl("/api/auth/login")
                );
        return http.build();
    }
}