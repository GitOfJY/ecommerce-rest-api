package com.sparta.demo.config;

import com.p6spy.engine.spy.P6SpyOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("dev")   // 👉 dev 프로필에서만 적용
@Configuration
public class P6spyConfig {
    @PostConstruct
    public void setLogMessageFormat() {
        P6SpyOptions.getActiveInstance()
                .setLogMessageFormat(P6spyPrettySqlFormatter.class.getName());
    }
}
