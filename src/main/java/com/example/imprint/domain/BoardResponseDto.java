package com.example.imprint.domain;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BoardResponseDto {

    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Entity -> DTO 변환
    public BoardResponseDto(BoardEntity boardEntity) {
        this.id = boardEntity.getId();
        this.name = boardEntity.getName();
        this.createdAt = boardEntity.getCreatedAt();
        this.updatedAt = boardEntity.getUpdatedAt();
    }
}
