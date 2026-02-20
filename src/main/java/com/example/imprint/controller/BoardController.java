package com.example.imprint.controller;

import com.example.imprint.domain.ApiResponseDto;
import com.example.imprint.domain.BoardRequestDto;
import com.example.imprint.domain.BoardResponseDto;
import com.example.imprint.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    // 게시판 생성
    @PostMapping
    public ResponseEntity<ApiResponseDto<Void>> create(
            @RequestBody BoardRequestDto requestDto,
            @RequestHeader("User-Id") Long userId
    ) {
        boardService.save(requestDto, userId);
        return ResponseEntity.ok(ApiResponseDto.success(null, "게시판이 생성되었습니다."));
    }

    // 전체 조회
    @GetMapping
    public ResponseEntity<ApiResponseDto<List<BoardResponseDto>>> findAll() {
        List<BoardResponseDto> boards = boardService.findAll();
        return ResponseEntity.ok(ApiResponseDto.success(boards, "전체 게시판 목록을 조회했습니다."));
    }

    // 단건 조회 (이름 기준)
    @GetMapping("/{name}")
    public ResponseEntity<ApiResponseDto<BoardResponseDto>> findByName(@PathVariable String name) {
        BoardResponseDto board = boardService.findByName(name);
        return ResponseEntity.ok(ApiResponseDto.success(board, "게시판 정보를 조회했습니다."));
    }

    // 수정 (Put -> Patch 변경 예정이라 하셔서 구조만 유지)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Long>> update(
            @PathVariable Long id,
            @RequestBody BoardRequestDto requestDto,
            @RequestHeader("User-Id") Long userId
    ) {
        Long updatedId = boardService.update(id, requestDto, userId);
        return ResponseEntity.ok(ApiResponseDto.success(updatedId, "게시판이 성공적으로 수정되었습니다."));
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> delete(
            @PathVariable Long id,
            @RequestHeader("User-Id") Long userId
    ) {
        boardService.delete(id, userId);
        return ResponseEntity.ok(ApiResponseDto.success(null, "게시판이 삭제되었습니다."));
    }
}