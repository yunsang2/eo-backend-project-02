package com.example.imprint.service.user;

import com.example.imprint.domain.user.UserEntity;
import com.example.imprint.domain.user.UserRole;
import com.example.imprint.domain.user.UserStatus;
import com.example.imprint.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
class PasswordFindTest {

    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    // 실제 메일 발송 방지용 가짜 객체
    @MockitoBean private MailService mailService;

    private final String TEST_EMAIL = "findtest@gmail.com";

    @BeforeEach
    void setUp() {
        // 테스트용 유저 미리 저장
        UserEntity user = UserEntity.builder()
                .email(TEST_EMAIL)
                .password(passwordEncoder.encode("oldPassword123!"))
                .nickname("찾기테스터")
                .name("김철수")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(user);
    }

    @Test
    @DisplayName("성공: 비밀번호 재설정 링크가 생성되고 메일이 발송되어야 한다")
    void requestPasswordResetSuccess() {
        // When
        userService.sendResetLink(TEST_EMAIL);

        // Then
        UserEntity user = userRepository.findByEmail(TEST_EMAIL).orElseThrow();
        // 토큰 생성 확인
        assertThat(user.getResetToken()).isNotNull();
        // 만료시간 확인
        assertThat(user.getTokenExpiry()).isAfter(LocalDateTime.now());

        // 메일 발송 메서드가 실제로 호출되었는지 검증
        verify(mailService).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("성공: 유효한 토큰으로 비밀번호를 변경하면 DB에 반영되고 토큰은 삭제되어야 한다")
    void resetPasswordCompleteSuccess() {
        // Given: 먼저 링크를 발송하여 토큰을 생성함
        userService.sendResetLink(TEST_EMAIL);
        UserEntity user = userRepository.findByEmail(TEST_EMAIL).orElseThrow();
        String validToken = user.getResetToken();
        String newPassword = "newPassword999#";

        // When: 새 비밀번호로 변경 요청
        userService.resetPassword(validToken, newPassword);

        // Then
        UserEntity updatedUser = userRepository.findByEmail(TEST_EMAIL).orElseThrow();
        // 암호화된 비밀번호 일치 확인
        assertThat(passwordEncoder.matches(newPassword, updatedUser.getPassword())).isTrue();
        // 사용 완료된 토큰은 Null이어야 함 (보안)
        assertThat(updatedUser.getResetToken()).isNull();
        assertThat(updatedUser.getTokenExpiry()).isNull();
    }

    @Test
    @DisplayName("실패: 만료된 토큰으로 변경 시도 시 예외가 발생해야 한다")
    void resetPasswordFailExpired() {
        // Given: 토큰 생성 후 만료 시간을 과거로 조작
        userService.sendResetLink(TEST_EMAIL);
        UserEntity user = userRepository.findByEmail(TEST_EMAIL).orElseThrow();
        user.setPasswordResetToken(user.getResetToken(), LocalDateTime.now().minusSeconds(1));
        userRepository.saveAndFlush(user);

        // When & Then
        assertThatThrownBy(() -> userService.resetPassword(user.getResetToken(), "newPass!"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("만료");
    }

    @Test
    @DisplayName("실패: 존재하지 않는 이메일로 링크 요청 시 예외가 발생해야 한다")
    void requestResetWithInvalidEmail() {
        // When & Then
        assertThatThrownBy(() -> userService.sendResetLink("none@example.com"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}