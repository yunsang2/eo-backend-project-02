package com.example.imprint.repository.message.report;

import com.example.imprint.domain.message.report.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, Long> {

    // 관리자가 최신순으로 모든 신고를 확인
    List<ReportEntity> findAllByOrderByIdDesc();

    // 최신순으로 정렬
    List<ReportEntity> findAllByOrderByCreatedAtDesc();

    // 관리자가 아직 읽지 않은(확인하지 않은) 신고 수 카운트
    long countByIsReadFalse();
}