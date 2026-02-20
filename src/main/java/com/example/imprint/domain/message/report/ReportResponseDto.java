package com.example.imprint.domain.message.report;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ReportResponseDto {
    private Long id;
    private Long targetUserId;
    private String reportCategory;
    private String content;
    private String reporterNickname;
    private LocalDateTime createdAt;

    public ReportResponseDto(ReportEntity entity) {
        this.id = entity.getId();
        this.targetUserId = entity.getTargetUserId();
        this.reportCategory = entity.getReportCategory();
        this.content = entity.getContent();
        this.reporterNickname = entity.getReporter().getNickname();
        this.createdAt = entity.getCreatedAt();
    }
}