package com.example.imprint.service.message;

import com.example.imprint.domain.message.report.ReportEntity;
import com.example.imprint.domain.user.UserEntity;
import com.example.imprint.domain.message.report.ReportResponseDto;
import com.example.imprint.domain.message.report.ReportRequestDto;
import com.example.imprint.repository.message.report.ReportRepository;
import com.example.imprint.repository.user.UserRepository;
import com.example.imprint.service.message.report.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @InjectMocks
    private ReportService reportService;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserRepository userRepository;

    private UserEntity reporter;
    private UserEntity targetUser;

    @BeforeEach
    void setUp() {
        // 신고자 (ID: 1)
        reporter = UserEntity.builder()
                .nickname("신고자")
                .build();
        ReflectionTestUtils.setField(reporter, "id", 1L);

        // 신고 대상자 (ID: 2)
        targetUser = UserEntity.builder()
                .nickname("불량유저")
                .build();
        ReflectionTestUtils.setField(targetUser, "id", 2L);
    }

    @Test
    @DisplayName("사용자가 다른 사용자를 신고하면 정상적으로 저장되어야 한다")
    void submitReportTest() {
        // given
        ReportRequestDto request = new ReportRequestDto(2L, "욕설", "게시판에서 욕을 함");

        when(userRepository.findById(1L)).thenReturn(Optional.of(reporter));
        when(userRepository.existsById(2L)).thenReturn(true);

        // when
        reportService.submitReport(1L, request);

        // then
        verify(reportRepository, times(1)).save(any(ReportEntity.class));
    }

    @Test
    @DisplayName("관리자는 모든 신고 내역을 최신순으로 조회할 수 있어야 한다")
    void getAllReportsForAdminTest() {
        // given
        ReportEntity report1 = ReportEntity.builder()
                .targetUserId(2L)
                .reportCategory("스팸")
                .reporter(reporter)
                .build();

        // Mocking: 리포지토리가 리스트를 반환한다고 가정
        when(reportRepository.findAllByOrderByIdDesc()).thenReturn(List.of(report1));

        // when
        List<ReportResponseDto> result = reportService.getAllReports();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getReportCategory()).isEqualTo("스팸");
        assertThat(result.get(0).getReporterNickname()).isEqualTo("신고자");
    }
}