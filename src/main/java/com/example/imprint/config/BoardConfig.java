package com.example.imprint.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class BoardConfig {

    @Bean
    public SecurityFilterChain securityfilterchain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 개발 단계: CSRF 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/boards/**").permitAll()    // 모든 접근 허용
                        .requestMatchers("/h2-console/**").permitAll()    //
                        .anyRequest().authenticated()               //나머지는 인증 필요
                )
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                );

        return http.build();
    }
}
