package com.example.imprint.service.user;

import com.example.imprint.domain.user.UserEntity;
import com.example.imprint.domain.user.UserSignupRequestDto;
import com.example.imprint.repository.user.EmailVerificationRepository;
import com.example.imprint.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmailVerificationRepository verificationRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final MailService mailService;

    @Transactional
    public void registerUser(UserSignupRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
        }

        boolean isVerified = verificationRepository.existsByEmailAndIsVerifiedTrue(request.getEmail());
        if (!isVerified) {
            throw new IllegalArgumentException("이메일 인증이 완료되지 않았습니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .nickname(request.getNickname())
                .name(request.getName())
                .build();

        user.activate();
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public void login(String email, String password) {
        // 이메일 존재 확인
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        // 비밀번호 비교 (평문 패스워드 = 암호화된 패스워드)
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
    }

    // 재설정 링크 메일 발송
    @Transactional
    public void sendResetLink(String email) {
        // 유저 존재 확인
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입된 회원이 없습니다."));

        // 토큰 생성 및 만료시간 설정 (10분)
        String token = UUID.randomUUID().toString();
        java.time.LocalDateTime expiry = java.time.LocalDateTime.now().plusMinutes(10);

        // 엔티티에 토큰 정보 저장
        user.setPasswordResetToken(token, expiry);

        // 메일 발송
        String resetLink = "http://localhost:8080/reset-password.html?token=" + token;
        String subject = "[Imprint] 비밀번호 재설정을 위한 링크입니다.";
        String text = user.getName() + "님, 안녕하세요.\n\n" +
                "아래 링크를 클릭하여 비밀번호를 재설정해 주세요. 본 요청은 10분간 유효합니다.\n" +
                resetLink + "\n\n" +
                "본인이 요청하지 않았다면 이 메일을 무시하셔도 됩니다.";

        mailService.sendEmail(email, subject, text);
    }

    // 토큰 검증 후 비밀번호 변경
    @Transactional
    public void resetPassword(String token, String newPassword) {
        // 토큰으로 유저 찾기
        UserEntity user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 접근이거나 만료된 링크입니다."));

        // 시간 만료 체크
        if (user.getTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
            user.clearResetToken(); // 만료된 토큰은 지워줌
            throw new IllegalArgumentException("비밀번호 재설정 유효 시간이 만료되었습니다.");
        }

        // 비밀번호 암호화 및 업데이트
        user.updatePassword(passwordEncoder.encode(newPassword));

        // 보안을 위해 사용한 토큰 즉시 제거
        user.clearResetToken();
    }
}