package com.jy.shoppy.global.security.dsl;

import com.jy.shoppy.global.security.filter.RestAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class RestApiDsl<H extends HttpSecurityBuilder<H>>
        extends AbstractAuthenticationFilterConfigurer<H, RestApiDsl<H>, RestAuthenticationFilter> {
    // RestAuthenticationFilter를 SecurityFilterChain에 등록

    private AuthenticationSuccessHandler successHandler;
    private AuthenticationFailureHandler failureHandler;

    public RestApiDsl() {
        super(new RestAuthenticationFilter("/api/auth/login"), null);
    }

    @Override
    public void configure(H http) throws Exception {
        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
        RestAuthenticationFilter filter = getAuthenticationFilter();

        filter.setAuthenticationManager(authenticationManager);
        filter.setAuthenticationSuccessHandler(successHandler);
        filter.setAuthenticationFailureHandler(failureHandler);
        filter.setSecurityContextRepository(new HttpSessionSecurityContextRepository());

        http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
    }

    public RestApiDsl<H> restSuccessHandler(AuthenticationSuccessHandler handler) {
        this.successHandler = handler;
        return this;
    }

    public RestApiDsl<H> restFailureHandler(AuthenticationFailureHandler handler) {
        this.failureHandler = handler;
        return this;
    }

    @Override
    protected RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl) {
        return PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, loginProcessingUrl);
    }
}