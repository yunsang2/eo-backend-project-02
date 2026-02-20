package com.example.imprint.domain.message.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestDto {
    // 신고 대상자 ID
    private Long targetUserId;
    // 신고 카테고리
    private String reportCategory;
    // 신고 내용
    private String content;
}
