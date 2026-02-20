package com.example.imprint.domain.user;

import lombok.Getter;
import java.util.Arrays;

@Getter
public enum UserStatus {
    PENDING(0, "대기 (인증 전)"), // "로그인 전"보다는 "인증 전"이 더 정확합니다!
    ACTIVE(1, "활성 (정상)"),
    BANNED(2, "정지 (차단됨)");

    private final int value;
    private final String description;

    UserStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static UserStatus fromValue(int value) {
        return Arrays.stream(UserStatus.values())
                .filter(v -> v.getValue() == value)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 상태: " + value));
    }
}