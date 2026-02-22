package com.example.imprint.controller.post;

import com.example.imprint.domain.ApiResponseDto;
import com.example.imprint.domain.page.CriteriaDto;
import com.example.imprint.domain.page.PaginationDto;
import com.example.imprint.domain.post.PostDto;
import com.example.imprint.service.post.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/boards/{boardId}/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<PostDto.IdResponse>> write(
            @PathVariable Long boardId,
            @RequestBody PostDto.Write dto) throws AccessDeniedException {
        Long id = postService.write(boardId, dto);

        return ResponseEntity.ok(
                ApiResponseDto.success(new PostDto.IdResponse(id), "게시물을 성공적으로 생성하였습니다.")
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<PostDto.pagedResponse>> findAll(
            @PathVariable Long boardId,
            CriteriaDto criteria) {

        Pageable pageable = PageRequest.of(criteria.getPage() - 1,
                criteria.getSize(), Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<PostDto.Response> postDtoPage = postService.getPostList(boardId, pageable);

        PaginationDto pagination = PaginationDto.of(pageable,
                postDtoPage.getTotalElements(), postDtoPage.getTotalPages());

        return ResponseEntity.ok(
                ApiResponseDto.success(
                        new PostDto.pagedResponse(postDtoPage.getContent(), pagination),
                        "게시물 목록을 성공적으로 조회하였습니다.")
        );
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponseDto<PostDto.Response>> findById(
            @PathVariable Long boardId,
            @PathVariable Long postId) {
        PostDto.Response post = postService.get(boardId, postId);

        return ResponseEntity.ok(
                ApiResponseDto.success(post, "게시물 정보를 조회했습니다.")
        );
    }

    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponseDto<PostDto.IdResponse>> update(
            @PathVariable Long boardId,
            @PathVariable Long postId,
            @RequestBody PostDto.Update dto) throws AccessDeniedException {
        Long updatedId = postService.update(boardId, postId, dto);

        return ResponseEntity.ok(
                ApiResponseDto.success(new PostDto.IdResponse(updatedId), "게시물이 성공적으로 수정되었습니다.")
        );
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponseDto<Void>> delete(
            @PathVariable Long boardId,
            @PathVariable Long postId) throws AccessDeniedException {
        postService.delete(boardId, postId);

        return ResponseEntity.ok(
                ApiResponseDto.success("게시물이 성공적으로 삭제되었습니다.")
        );
    }
}