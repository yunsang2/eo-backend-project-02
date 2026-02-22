package com.example.imprint.domain.board;

import com.example.imprint.domain.page.PaginationDto;

import java.time.LocalDateTime;
import java.util.List;

public class BoardDto {
    public record Create(String name) {}
    public record Update(String name) {}
    public record ManagerRequest(Long id) {}

    public record Response(
            Long id,
            String name,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {}

    public record IdResponse(Long id) {}

    public record PagedResponse(
            List<Response> boardList,
            PaginationDto pagination) {}
}
