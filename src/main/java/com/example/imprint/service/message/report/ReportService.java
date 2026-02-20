package com.example.imprint.service.message.report;

import com.example.imprint.domain.message.report.ReportEntity;
import com.example.imprint.domain.message.report.ReportRequestDto;
import com.example.imprint.domain.message.report.ReportResponseDto;
import com.example.imprint.domain.user.UserEntity;
import com.example.imprint.repository.message.report.ReportRepository;
import com.example.imprint.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    // 신고 접수
    @Transactional
    public void submitReport(Long reporterId, ReportRequestDto request) {
        UserEntity reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new IllegalArgumentException("신고자를 찾을 수 없습니다."));

        if (!userRepository.existsById(request.getTargetUserId())) {
            throw new IllegalArgumentException("신고 대상 유저가 존재하지 않습니다.");
        }

        ReportEntity report = ReportEntity.builder()
                .targetUserId(request.getTargetUserId())
                .reportCategory(request.getReportCategory())
                .content(request.getContent())
                .reporter(reporter)
                .build();

        reportRepository.save(report);
    }

    // 관리자용 전체 목록 조회
    @Transactional(readOnly = true)
    public List<ReportResponseDto> getAllReports() {
        return reportRepository.findAllByOrderByIdDesc().stream()
                .map(ReportResponseDto::new)
                .toList();
    }
}