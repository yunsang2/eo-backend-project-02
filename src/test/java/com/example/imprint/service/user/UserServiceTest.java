package com.example.imprint.service.user;

import com.example.imprint.domain.user.UserSignupRequestDto;
import com.example.imprint.repository.user.EmailVerificationRepository;
import com.example.imprint.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationRepository verificationRepository;

    @Spy // 실제 암호화 로직을 타야할땐 Spy 사용
    private BCryptPasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원가입 성공 테스트")
    void registerUser_Success() {

        UserSignupRequestDto request = createRequestDto();

        // Mock 설정: 중복 없고, 이메일 인증 완료됨
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByNickname(any())).thenReturn(false);
        when(verificationRepository.existsByEmailAndIsVerifiedTrue(any())).thenReturn(true);

        userService.registerUser(request);

        // save가 한 번 호출되었는지 확인
        verify(userRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("이메일 인증 안됐을 때 가입 실패 테스트")
    void registerUser_Fail_NotVerified() {

        UserSignupRequestDto request = createRequestDto();
        when(verificationRepository.existsByEmailAndIsVerifiedTrue(any())).thenReturn(false);


        assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(request);
        }, "이메일 인증이 완료되지 않았습니다.");
    }

    private UserSignupRequestDto createRequestDto() {
        return new UserSignupRequestDto("test@gmail.com", "password123!", "tester", "홍길동");
    }
}