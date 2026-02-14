package com.example.imprint.domain;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BoardResponseDto {

    private final Long id;
    private final String name;

    // 만든이 정보
    private Long creatorId;
    private String creatorName;
    private String creatorNickname;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Entity -> DTO 변환
    public BoardResponseDto(BoardEntity boardEntity) {
        this.id = boardEntity.getId();
        this.name = boardEntity.getName();

        // 만든이 정보 추출 (매니저로 바꾼다.)
        this.creatorId = boardEntity.getCreator().getId();
        this.creatorName = boardEntity.getCreator().getName();
        this.creatorNickname = boardEntity.getCreator().getNickname();

        this.createdAt = boardEntity.getCreatedAt();
        this.updatedAt = boardEntity.getUpdatedAt();
    }
}
