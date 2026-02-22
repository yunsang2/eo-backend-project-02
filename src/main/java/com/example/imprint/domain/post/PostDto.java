package com.example.imprint.domain.post;

import com.example.imprint.domain.page.PaginationDto;

import java.time.LocalDateTime;
import java.util.List;

public class PostDto {
    public record Write(
            String title,
            String content) {}

    public record Update(
            String title,
            String content) {}

    public record Response(
            Long id,
            Long boardId,
            Long writerId,
            String title,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {}

    public record IdResponse(Long id) {}

    public record pagedResponse(
            List<Response> postList,
            PaginationDto pagination) {}
}
