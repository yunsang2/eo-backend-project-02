package com.example.imprint.controller.message;

import com.example.imprint.domain.ApiResponseDto;
import com.example.imprint.domain.message.report.ReportResponseDto;
import com.example.imprint.domain.message.report.ReportRequestDto;
import com.example.imprint.service.message.report.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // [사용자] 신고 보내기
    @PostMapping("/api/reports")
    public ResponseEntity<ApiResponseDto<Void>> submitReport(
            @AuthenticationPrincipal Long reporterId,
            @RequestBody ReportRequestDto request) {
        reportService.submitReport(reporterId, request);
        return ResponseEntity.ok(ApiResponseDto.success("신고가 접수되었습니다."));
    }

    // [관리자] 신고 대시보드 목록 조회
    @GetMapping("/api/admin/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<List<ReportResponseDto>>> getAdminReports() {
        return ResponseEntity.ok(ApiResponseDto.success(reportService.getAllReports()));
    }
}