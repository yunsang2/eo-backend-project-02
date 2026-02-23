package com.example.imprint.config;

import com.example.imprint.domain.ApiResponseDto;
import com.example.imprint.security.user.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    // 비밀번호 암호화 빈 등록 (로그인 시 비밀번호 비교에 사용됨)
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(form -> form
                        .loginProcessingUrl("/account/login")
                        .successHandler((request, response, authentication) -> {
                            response.setContentType("application/json;");
                            response.setCharacterEncoding("UTF-8");
                            response.setStatus(HttpServletResponse.SC_OK);

                            ApiResponseDto<Void> body = ApiResponseDto.success("로그인에 성공했습니다.");
                            response.getWriter().write(new ObjectMapper().writeValueAsString(body));
                        })
                        .failureHandler((request, response, exception) -> {
                            response.setContentType("application/json;");
                            response.setCharacterEncoding("UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                            ApiResponseDto<Void> body = ApiResponseDto.fail(exception.getMessage());
                            response.getWriter().write(new ObjectMapper().writeValueAsString(body));
                        })
                )
                .logout(logout -> logout
                        .logoutUrl("/account/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setContentType("application/json;");
                            response.setCharacterEncoding("UTF-8");
                            response.setStatus(HttpServletResponse.SC_OK);

                            ApiResponseDto<Void> body = ApiResponseDto.success("로그아웃에 성공했습니다.");
                            response.getWriter().write(new ObjectMapper().writeValueAsString(body));
                        })
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .authorizeHttpRequests(auth -> auth
                        // 회원가입 및 이메일 인증 로그인 관련 API 모두 허용
                        .requestMatchers("/api/mail/**", "/account/**").permitAll()
                        // 정적 리소스 및 화면 주소 허용
                        .requestMatchers("/index.html", "/css/**", "/javascript/**", "/images/**", "/favicon.ico", "/error").permitAll()
                        .requestMatchers("/", "/index").permitAll()
                        .requestMatchers(HttpMethod.POST, "/boards").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/boards/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/boards/{id}").hasRole("ADMIN")
                        .requestMatchers("/boards/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(true)
                );

        return http.build();
    }
}