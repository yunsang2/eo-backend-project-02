package com.example.imprint.config;

import com.example.imprint.security.user.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    // 비밀번호 암호화 빈 등록 (로그인 시 비밀번호 비교에 사용됨)
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        // 회원가입 및 이메일 인증 관련 API 모두 허용
                        .requestMatchers("/api/mail/**", "/account/signup").permitAll()
                        // 로그인 API 주소 허용
                        .requestMatchers("/account/login").permitAll()
                        // 정적 리소스 및 화면 주소 허용
                        .requestMatchers("/", "/test.html", "/loginForm.html", "/main.html").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/api/boards/**").permitAll()    // 모든 접근 허용
                        .requestMatchers("/h2-console/**").permitAll()    //
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/loginForm.html")
                        // 프론트가 POST를 보낼 위치
                        .loginProcessingUrl("/account/login")
                        // 필드명 일치
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/main.html", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/account/logout")
                        .logoutSuccessUrl("/loginForm.html")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );

            return http.build();
    }
}