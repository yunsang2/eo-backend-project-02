package com.example.imprint.domain.user;


import com.example.imprint.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "USERS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 15)
    private String nickname;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING;

    // --- 비밀번호 재설정 관련 필드 추가 ---
    @Column(length = 100)
    private String resetToken;

    private java.time.LocalDateTime tokenExpiry;


    // --- 유저 상태, 권한 로직 메서드 ---
    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public void ban() {
        this.status = UserStatus.BANNED;
    }

    public void changeRole(UserRole newRole) {
        this.role = newRole;
    }


    // --- 유저 비밀번호 변경 로직 메서드 ---
    // 비밀번호 변경 (암호화된 비번을 받음)
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    // 재설정 토큰 발행
    public void setPasswordResetToken(String token, java.time.LocalDateTime expiry) {
        this.resetToken = token;
        this.tokenExpiry = expiry;
    }

    // 재설정 완료 후 토큰 초기화
    public void clearResetToken() {
        this.resetToken = null;
        this.tokenExpiry = null;
    }
}