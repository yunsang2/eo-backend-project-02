package com.example.imprint.domain.board;

public class BoardMapper {
    public static BoardEntity fromDtoToEntity(BoardDto.Create dto) {
        return new BoardEntity(dto.name());
    }

    public static BoardDto.Response fromEntityToDto(BoardEntity entity) {
        return new BoardDto.Response(
                entity.getId(),
                entity.getName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
