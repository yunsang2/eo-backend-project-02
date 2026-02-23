package com.example.imprint.controller.user;

import com.example.imprint.domain.ApiResponseDto;
import com.example.imprint.domain.post.PostDto;
import com.example.imprint.service.post.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class MainController {
    private final PostService postService;

    // 통합 검색 API (제목 + 내용 + 작성자)
    @GetMapping("/search")
    public ResponseEntity<ApiResponseDto<PostDto.pagedResponse>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        // 검색어가 비어있을 경우 방어 로직
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("검색어를 입력해주세요.");
        }

        PostDto.pagedResponse searchResult = postService.searchPosts(keyword, page, size);

        return ResponseEntity.ok(ApiResponseDto.success(searchResult, "통합 검색 결과를 성공적으로 불러왔습니다."));
    }
}
