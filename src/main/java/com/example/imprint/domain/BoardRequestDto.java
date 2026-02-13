package com.example.imprint.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BoardRequestDto {

    private String name; // 게시판 이름

    public BoardEntity toEntity() { // DTO -> Entity 변환
        return BoardEntity.builder()
                .name(name)
                .build();
    }
}
