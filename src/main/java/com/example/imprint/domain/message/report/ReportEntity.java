package com.example.imprint.domain.message.report;

import com.example.imprint.domain.BaseTimeEntity;
import com.example.imprint.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "target_id", nullable = false)
    // 신고 대상 유저 ID
    private Long targetUserId;

    @Column(name = "report_category", nullable = false)
    // 항목 체크형 카테고리
    private String reportCategory;

    @Column(length = 1000)
    // 상세 사유
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    // 신고자
    private UserEntity reporter;

    // 확인 여부
    private boolean isRead = false;

    @Builder
    public ReportEntity(Long targetUserId, String reportCategory, String content, UserEntity reporter) {
        this.targetUserId = targetUserId;
        this.reportCategory = reportCategory;
        this.content = content;
        this.reporter = reporter;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}