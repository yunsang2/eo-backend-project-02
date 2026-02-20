package com.example.imprint.controller.user;

import com.example.imprint.domain.user.UserEntity;
import com.example.imprint.domain.user.UserLoginRequestDto;
import com.example.imprint.domain.user.UserRole;
import com.example.imprint.domain.user.UserStatus;
import com.example.imprint.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserLoginTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // 테스트용 유저 생성 (중복 제거 및 필수 필드 추가)
        if (userRepository.findByEmail("test@gmail.com").isEmpty()) {
            UserEntity testUser = UserEntity.builder()
                    .email("test@gmail.com")
                    .password(passwordEncoder.encode("1234"))
                    .nickname("테스터")
                    .name("홍길동")
                    .role(UserRole.USER) // CustomUserDetails의 getAuthorities용
                    .status(UserStatus.ACTIVE)    // CustomUserDetails의 BANNED 체크 통과용
                    .build();
            userRepository.save(testUser);
        }
    }

    @Test
    @DisplayName("로그인이 성공하면 JSON 응답과 200 OK를 반환해야 한다")
    void loginSuccessTest() throws Exception {
        // Given: JSON 데이터 생성
        UserLoginRequestDto loginDto = new UserLoginRequestDto("test@gmail.com", "1234");
        String json = objectMapper.writeValueAsString(loginDto);

        // When & Then
        mockMvc.perform(post("/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그인이 성공적으로 완료되었습니다."));
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 400 에러를 반환해야 한다")
    void loginFailTest() throws Exception {
        // Given: 틀린 비밀번호 JSON
        UserLoginRequestDto loginDto = new UserLoginRequestDto("test@gmail.com", "wrong_password");
        String json = objectMapper.writeValueAsString(loginDto);

        // When & Then
        mockMvc.perform(post("/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("로그인 후 내 정보 조회 API(200 OK) 확인")
    @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void authenticationCheckTest() throws Exception {
        mockMvc.perform(get("/account/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@gmail.com"))
                .andExpect(jsonPath("$.data.nickname").value("테스터"));
    }

    @Test
    @DisplayName("로그아웃 시 세션이 만료되어야 한다")
    void logoutTest() throws Exception {
        mockMvc.perform(post("/account/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/loginForm.html"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("로그아웃 후에는 보호된 리소스에 접근할 수 없어야 한다")
    void logoutIntegrationTest() throws Exception {
        // 먼저 로그인을 시뮬레이션
        mockMvc.perform(formLogin("/account/login")
                .user("email", "test@gmail.com")
                .password("password", "1234"));

        // 로그아웃 수행
        mockMvc.perform(post("/account/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/loginForm.html"));

        // 로그아웃 되었으므로 내 정보(/account/user)를 요청하면 접근이 거부되어야 함
        mockMvc.perform(get("/account/user"))
                .andExpect(status().isUnauthorized());
    }

}
