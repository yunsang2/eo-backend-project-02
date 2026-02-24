package com.example.imprint.service.user;

import com.example.imprint.domain.user.EmailVerification;
import com.example.imprint.repository.user.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final EmailVerificationRepository verificationRepository;

    @Transactional
    public void sendCodeToEmail(String email) {
        String cleanEmail = email.trim().replace("\"", "");

        // 중복 요청 제한 확인 (최근 1분 이내 기록 확인)
        verificationRepository.findTopByEmailOrderByCreatedAtDesc(cleanEmail)
                .ifPresent(existing -> {
                    if (existing.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(1))) {
                        throw new IllegalArgumentException("1분 이내에 이미 인증번호를 요청했습니다. 잠시 후 다시 시도해주세요.");
                    }
                });

        String authCode = createCode();

        // 이메일 발송 (발송 실패 시 DB 저장이 되지 않도록 먼저 수행 권장)
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(cleanEmail);
        message.setFrom("ImPrint");
        message.setSubject("[Imprint] 회원가입 인증번호입니다.");
        message.setText("인증번호: " + authCode + "\n5분 이내에 입력해주세요.");

        mailSender.send(message);

        // 인증 정보 저장 (기존 데이터가 있다면 삭제 후 저장하거나 업데이트)
        // 여기서는 깔끔하게 새로 저장하되, 나중에 UserService 가입 완료 시점에 일괄 삭제를 권장합니다.
        EmailVerification verification = EmailVerification.builder()
                .email(cleanEmail)
                .authCode(authCode)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();

        verificationRepository.save(verification);
    }

    @Transactional
    public boolean verifyCode(String email, String code) {
        EmailVerification verification = verificationRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new IllegalArgumentException("인증 요청 기록이 없습니다."));

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("인증 시간이 만료되었습니다.");
        }
        if (!verification.getAuthCode().equals(code)) {
            throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
        }

        verification.verify();
        return true;
    }

    private String createCode() {
        return String.valueOf(new Random().nextInt(899999) + 100000);
    }

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to.trim().replace("\"", "")); // 이메일 따옴표 및 공백 제거
        message.setFrom("ImPrint");
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
}