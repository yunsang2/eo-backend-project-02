package com.example.imprint.controller.user;

import com.example.imprint.domain.ApiResponseDto;
import com.example.imprint.domain.admin.DashboardResponseDto;
import com.example.imprint.domain.message.MessageResponseDto;
import com.example.imprint.domain.message.report.ReportResponseDto;
import com.example.imprint.domain.user.UserRole;
import com.example.imprint.domain.user.UserStatus;
import com.example.imprint.security.user.CustomUserDetails;
import com.example.imprint.service.admin.DashboardService;
import com.example.imprint.service.admin.AdminService;
import com.example.imprint.service.message.MessageService;
import com.example.imprint.service.message.report.ReportService;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
// 관리자만 해당 url로 접근 가능
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final DashboardService DashboardService;
    private final MessageService messageService;
    private final ReportService reportService;

    // 대시보드 메인 오버뷰 데이터 조회
    @GetMapping("/overview")
    public ResponseEntity<ApiResponseDto<DashboardResponseDto>> getDashboardOverview() {
        DashboardResponseDto overview = DashboardService.getOverview();
        return ResponseEntity.ok(ApiResponseDto.success(overview));
    }

    // 유저 상태 수정 ( ACTIVE ↔ BANNED )
    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<ApiResponseDto<Void>> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody StatusUpdateRequest request) {

        adminService.updateUserStatus(userId, request.getNewStatus());
        return ResponseEntity.ok(ApiResponseDto.success("사용자의 상태가 성공적으로 변경되었습니다."));
    }

    // 관리자에게 온 문의 목록 조회
    @GetMapping("/supports")
    public ResponseEntity<ApiResponseDto<List<MessageResponseDto>>> getAllSupports() {
        List<MessageResponseDto> supports = messageService.getAdminSupports();
        return ResponseEntity.ok(ApiResponseDto.success(supports));
    }

    // 특정 문의 상세 내용 조회 및 읽음 처리
    @GetMapping("/supports/{supportId}")
    public ResponseEntity<ApiResponseDto<MessageResponseDto>> getSupport(
            @PathVariable Long supportId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        MessageResponseDto detail = messageService.readMessage(supportId, userDetails.getUser().getEmail());

        return ResponseEntity.ok(ApiResponseDto.success(detail));
    }

    // 관리자에게 온 신고 목록 조회
    @GetMapping("/reports")
    public ResponseEntity<ApiResponseDto<List<ReportResponseDto>>> getAllReports() {
        return ResponseEntity.ok(ApiResponseDto.success(reportService.getAllReports()));
    }

    // 특정 신고 상세 내용 조회 및 읽음 처리
    @GetMapping("/reports/{reportId}")
    public ResponseEntity<ApiResponseDto<ReportResponseDto>> getReport(@PathVariable Long reportId) {
        return ResponseEntity.ok(ApiResponseDto.success(reportService.getReportDetail(reportId)));
    }


    // --- 내부 데이터 전달용 DTO( body에 담는 용도 ) ---

    @Getter
    @NoArgsConstructor
    public static class StatusUpdateRequest {
        private UserStatus newStatus;
    }

    @Getter
    @NoArgsConstructor
    public static class RoleUpdateRequest {
        private UserRole newRole;
    }

    // --- 대시보드 응답용 DTO ---
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DashboardOverviewResponse {
        // 총 가입자 수
        private long totalUserCount;
        // 금일 신규 가입자
        private long todaySignupCount;
        // 금일 게시글 수
//        private long todayPostCount;
        // 금일 댓글 수
//        private long todayCommentCount;
        // 미확인 신고 건수
        private long pendingReportCount;
    }
}