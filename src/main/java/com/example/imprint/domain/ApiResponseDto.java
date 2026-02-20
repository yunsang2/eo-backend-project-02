package com.example.imprint.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponseDto<T> {
    // 성공 여부 (true/false)
    private boolean success;
    // 실제 데이터 (성공 시에만 들어감)
    private T data;
    // 메시지 (성공 안내 또는 에러 메시지)
    private String message;

    // 성공 응답 정적 팩토리 메서드
    public static <T> ApiResponseDto<T> success(T data) {
        return new ApiResponseDto<>(true, data, "요청이 성공적으로 처리되었습니다.");
    }

    public static <T> ApiResponseDto<T> success(T data, String message) {
        return new ApiResponseDto<>(true, data, message);
    }

    // 데이터 없이 메시지만 보내는 성공 응답 (회원가입 완료 등)
    public static ApiResponseDto<Void> success(String message) {
        return new ApiResponseDto<>(true, null, message);
    }

    // 실패 응답 정적 팩토리 메서드
    public static <T> ApiResponseDto<T> fail(String message) {
        return new ApiResponseDto<>(false, null, message);
    }
}