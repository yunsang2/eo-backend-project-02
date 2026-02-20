package com.example.imprint.controller.user;

import com.example.imprint.domain.ApiResponseDto;
import com.example.imprint.domain.user.UserRole;
import com.example.imprint.service.user.AdminService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
// 관리자만 해당 url로 접근 가능
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // 유저 직급 수정 ( ADMIN ↔ MANAGER ↔ USER )
    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<ApiResponseDto<Void>> updateUserRole(
            @PathVariable Long userId,
            @RequestBody RoleUpdateRequest request) {

        adminService.updateUserRole(userId, request.getNewRole());
        return ResponseEntity.ok(ApiResponseDto.success("사용자의 권한이 성공적으로 변경되었습니다."));
    }

    // 특정 게시판의 매니저로 임명
    @PostMapping("/boards/{boardId}/managers/{userId}")
    public ResponseEntity<ApiResponseDto<Void>> assignManager(
            @PathVariable Long boardId,
            @PathVariable Long userId) {

        adminService.assignManager(userId, boardId);
        return ResponseEntity.ok(ApiResponseDto.success("해당 사용자가 게시판 매니저로 임명되었습니다."));
    }

    // 매니저 권한 회수 및 강등
    @DeleteMapping("/boards/{boardId}/managers/{userId}")
    public ResponseEntity<ApiResponseDto<Void>> dismissManager(
            @PathVariable Long boardId,
            @PathVariable Long userId) {

        adminService.dismissManager(userId, boardId);
        return ResponseEntity.ok(ApiResponseDto.success("해당 사용자의 매니저 권한이 회수되었습니다."));
    }


    // --- 내부 데이터 전달용 DTO( body에 담는 용도 ) ---

    @Getter
    @NoArgsConstructor
    public static class RoleUpdateRequest {
        private UserRole newRole;
    }
}