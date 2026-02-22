package com.example.imprint.domain.comment;

import com.example.imprint.domain.page.PaginationDto;

import java.time.LocalDateTime;
import java.util.List;

public class CommentDto {
    public record Write(String content) {}
    public record Update(String content) {}

    public record Response(
            Long id,
            Long postId,
            Long writerId,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {}

    public record IdResponse(Long id) {}

    public record PagedResponse(
            List<Response> commentList,
            PaginationDto pagination) {}
}
