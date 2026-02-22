package com.example.imprint.domain.admin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardResponseDto {
    private long totalUserCount;
    private long todaySignupCount;
    private long activeUserCount;
    private long bannedUserCount;
    private long todayPostCount;
    private long todayCommentCount;
    private long pendingReportCount;
    private long pendingSupportCount;
}
