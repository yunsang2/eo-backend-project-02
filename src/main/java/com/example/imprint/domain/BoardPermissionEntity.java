package com.example.imprint.domain;

import com.example.imprint.domain.user.UserEntity;
import com.example.imprint.domain.user.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.*;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "User_permissions")
@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardPermissionEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 게시판인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board.id", nullable = false)
    private BoardEntity board;

    // 어떤 유저인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "User.id", nullable = false)
    private UserEntity user;

    // 역할
    @Column(nullable = false)
    @Builder.Default
    private UserRole isManager = UserRole.USER;
    private UserRole isAdmin = UserRole.USER;

    public boolean isAdmin() {
        return isAdmin == UserRole.ADMIN;
    }

    public boolean isMANAGER() {
        return isManager == UserRole.MANAGER || isAdmin == UserRole.ADMIN;
    }
}
