package com.example.imprint.domain.post;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PostMapper {
    public static PostDto.Response fromEntityToDto(PostEntity entity) {
        return new PostDto.Response(
                entity.getId(),
                entity.getBoard().getId(),
                entity.getWriter().getId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
