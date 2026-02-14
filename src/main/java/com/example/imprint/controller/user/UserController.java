package com.example.imprint.controller.user;

import com.example.imprint.domain.ApiResponseDto;
import com.example.imprint.domain.user.UserEntity;
import com.example.imprint.domain.user.UserJoinRequestDto;
import com.example.imprint.domain.user.UserLoginRequestDto;
import com.example.imprint.domain.user.UserResponseDto;
import com.example.imprint.security.user.CustomUserDetails;
import com.example.imprint.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponseDto<Void>> join(@RequestBody @Valid UserJoinRequestDto requestDto) {
        userService.registerUser(requestDto);
        return ResponseEntity.ok(ApiResponseDto.success(null, "회원가입이 성공적으로 완료되었습니다."));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<Void>> login(@RequestBody @Valid UserLoginRequestDto loginDto, HttpServletRequest request) {
        userService.login(loginDto.getEmail(), loginDto.getPassword());

        HttpSession session = request.getSession();
        session.setAttribute("userEmail", loginDto.getEmail());

        return ResponseEntity.ok(ApiResponseDto.success(null, "로그인이 성공적으로 완료되었습니다."));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDto<Void>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(ApiResponseDto.success(null, "로그아웃 되었습니다."));
    }

    // 내 정보 조회 (핵심 데이터 반환)
    @GetMapping("/user")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        if (customUserDetails == null) {
            return ResponseEntity.status(401).body(ApiResponseDto.fail("로그인이 필요합니다."));
        }

        UserEntity user = customUserDetails.getUser();
        return ResponseEntity.ok(ApiResponseDto.success(UserResponseDto.fromEntity(user), "내 정보를 성공적으로 불러왔습니다."));
    }
}