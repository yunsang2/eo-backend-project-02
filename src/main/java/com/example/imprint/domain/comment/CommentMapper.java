package com.example.imprint.domain.comment;

public class CommentMapper {
    public static CommentDto.Response fromEntityToDto(CommentEntity entity) {
        return new CommentDto.Response(
                entity.getId(),
                entity.getPost().getId(),
                entity.getWriter().getId(),
                entity.getContent(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
