package com.example.imprint.service.user;

import com.example.imprint.domain.user.*;
import com.example.imprint.repository.user.EmailVerificationRepository;
import com.example.imprint.repository.user.UserRepository;
import com.example.imprint.security.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

        // 현재 DB에 저장된 전체 유저 수를 카운트
        long userCount = userRepository.count();

        // 유저가 0명이면 ADMIN, 1명이라도 있으면 USER 직급 부여
        UserRole assignedRole = (userCount == 0) ? UserRole.ADMIN : UserRole.USER;

        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .nickname(request.getNickname())
                .name(request.getName())
                .role(assignedRole)
                .build();

        user.activate();
        userRepository.save(user);
    }

    public UserResponseDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }

        UserEntity user = userRepository.findById(((CustomUserDetails) authentication.getPrincipal()).getUser().getId()).orElseThrow(
                () -> new IllegalArgumentException("흐음")
        );

        return UserResponseDto.fromEntity(user);
    }

    @Transactional
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
        String resetLink = "http://localhost:8080/?form=5&token=" + token;
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

    public boolean isActive(Long id) {
        UserEntity user = userRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("사용자를 찾을 수 없습니다. (id: " + id + ")")
        );

        return user.getStatus().equals(UserStatus.ACTIVE);
    }

    // 회원 탈퇴
    @Transactional
    public void withdrawUser(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 이미 탈퇴한 회원인지 방어 로직
        if (user.getStatus() == UserStatus.DELETED) {
            throw new IllegalArgumentException("이미 탈퇴 처리된 회원입니다.");
        }

        // 상태를 DELETED로 변경 (Soft Delete)
        user.delete();
    }

    // 정보 수정(이름, 별명)
    @Transactional
    public void updateUserInfo(String email, UserUpdateRequestDto request) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 닉네임을 변경하려는 경우에만 중복 체크 진행
        if (!user.getNickname().equals(request.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
        }

        // 엔티티 값 변경 (Dirty Checking으로 자동 UPDATE 됨)
        user.updateProfile(request.getNickname(), request.getName());
    }
}