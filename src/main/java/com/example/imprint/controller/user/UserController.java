package com.example.imprint.controller.user;

import com.example.imprint.domain.ApiResponseDto;
import com.example.imprint.domain.user.UserEntity;
import com.example.imprint.domain.user.UserSignupRequestDto;
import com.example.imprint.domain.user.UserLoginRequestDto;
import com.example.imprint.domain.user.UserResponseDto;
import com.example.imprint.security.user.CustomUserDetails;
import com.example.imprint.security.user.CustomUserDetailsService;
import com.example.imprint.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CustomUserDetailsService customUserDetailsService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponseDto<Void>> join(@RequestBody @Valid UserSignupRequestDto requestDto) {
        userService.registerUser(requestDto);
        return ResponseEntity.ok(ApiResponseDto.success(null, "회원가입이 성공적으로 완료되었습니다."));
    }

    // 내 정보 조회 (핵심 데이터 반환)
    @GetMapping({ "", "/user" })
    public ResponseEntity<ApiResponseDto<UserResponseDto>> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        if (customUserDetails == null) {
            return ResponseEntity.status(401).body(ApiResponseDto.fail("로그인이 필요합니다."));
        }

        UserEntity user = customUserDetails.getUser();
        return ResponseEntity.ok(ApiResponseDto.success(UserResponseDto.fromEntity(user), "내 정보를 성공적으로 불러왔습니다."));
    }

    // 비밀번호 재설정 링크 발송 API
    // 유저로부터 이메일을 받아 가입 여부 확인 후 메일을 보냄
    @PostMapping("/password/send-link")
    public ResponseEntity<ApiResponseDto<Void>> sendResetLink(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }

        userService.sendResetLink(email);

        return ResponseEntity.ok(ApiResponseDto.success(null, "비밀번호 재설정 링크가 이메일로 발송되었습니다."));
    }

    // 비밀번호 재설정 실행 API
    // 메일 링크에 담긴 토큰과 유저가 새로 입력한 비밀번호를 받아 처리
    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponseDto<Void>> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("새 비밀번호를 입력해주세요.");
        }

        userService.resetPassword(token, newPassword);

        return ResponseEntity.ok(ApiResponseDto.success(null, "비밀번호가 성공적으로 변경되었습니다."));
    }
}