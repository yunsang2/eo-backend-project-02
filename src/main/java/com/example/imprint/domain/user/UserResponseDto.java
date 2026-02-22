package com.example.imprint.domain.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {
    private Long id;
    private String email;
    private String nickname;
    private String name;
    private UserRole role;
    private UserStatus status;

    // 반환되는 값들
    public static UserResponseDto fromEntity(UserEntity user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .name(user.getName())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }
}