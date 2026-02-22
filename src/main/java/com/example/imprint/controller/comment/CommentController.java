package com.example.imprint.controller.comment;

import com.example.imprint.domain.ApiResponseDto;
import com.example.imprint.domain.comment.CommentDto;
import com.example.imprint.domain.page.CriteriaDto;
import com.example.imprint.domain.page.PaginationDto;
import com.example.imprint.service.comment.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/boards/{boardId}/posts/{postId}/comments")
@RequiredArgsConstructor
class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<CommentDto.IdResponse>> write(
            @PathVariable Long postId,
            @RequestBody CommentDto.Write dto) throws AccessDeniedException {

        Long id = commentService.write(postId, dto);

        return ResponseEntity.ok(
                ApiResponseDto.success(new CommentDto.IdResponse(id), "댓글을 성공적으로 작성하였습니다.")
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<CommentDto.PagedResponse>> findAll(
            @PathVariable Long postId,
            CriteriaDto criteria) {

        // 페이징 설정 (보통 댓글은 생성순 DESC 또는 ASC로 정렬)
        Pageable pageable = PageRequest.of(criteria.getPage() - 1,
                criteria.getSize(), Sort.by(Sort.Direction.ASC, "createdAt"));

        Page<CommentDto.Response> commentDtoPage = commentService.getCommentList(postId, pageable);

        PaginationDto pagination = PaginationDto.of(pageable,
                commentDtoPage.getTotalElements(), commentDtoPage.getTotalPages());

        return ResponseEntity.ok(
                ApiResponseDto.success(
                        new CommentDto.PagedResponse(commentDtoPage.getContent(), pagination),
                        "댓글 목록을 성공적으로 조회하였습니다.")
        );
    }

    // 댓글 단건 조회가 필요한 경우 (보통은 목록으로 보지만 기능상 추가)
    @GetMapping("/{commentId}")
    public ResponseEntity<ApiResponseDto<CommentDto.Response>> findById(
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        CommentDto.Response comment = commentService.get(postId, commentId);

        return ResponseEntity.ok(
                ApiResponseDto.success(comment, "댓글 정보를 조회했습니다.")
        );
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponseDto<CommentDto.IdResponse>> update(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody CommentDto.Update dto) throws AccessDeniedException {

        Long updatedId = commentService.update(postId, commentId, dto);

        return ResponseEntity.ok(
                ApiResponseDto.success(new CommentDto.IdResponse(updatedId), "댓글이 성공적으로 수정되었습니다.")
        );
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponseDto<Void>> delete(
            @PathVariable Long postId,
            @PathVariable Long commentId) throws AccessDeniedException {

        commentService.delete(postId, commentId);

        return ResponseEntity.ok(
                ApiResponseDto.success("댓글이 성공적으로 삭제되었습니다.")
        );
    }
}
