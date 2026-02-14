package com.example.imprint.controller;

import com.example.imprint.domain.BoardRequestDto;
import com.example.imprint.domain.BoardResponseDto;
import com.example.imprint.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController //REST API 컨트롤러
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;


    // 게시판 생성 - POST /boards
    @PostMapping
    public ResponseEntity<Long> create(
            @RequestBody BoardRequestDto requestDto,
            @RequestHeader("User-id") Long userId // 임시: 헤더로 유저 ID 받기
    ) {
        BoardResponseDto board = boardService.save(requestDto, userId);
        return ResponseEntity.ok(board.getId());
    }

    // 전체 조회 - Get /boards
    @GetMapping
    public ResponseEntity<List<BoardResponseDto>> findAll() {
        return ResponseEntity.ok(boardService.findAll());
    }

    // 단건 조회 - GET /boards/{id}
    @GetMapping("/{id}")
    public ResponseEntity<BoardResponseDto> findByName(@PathVariable String name) {
        return ResponseEntity.ok(boardService.findByName(name));
    }

    // 수정 - PUT /boards/{id}   ----- 패치방식으로 바꿀 예정
    @PutMapping("/{id}")
    public ResponseEntity<Long> update(
            @PathVariable Long id,
            @RequestBody BoardRequestDto requestDto,
            @RequestHeader("User-Id") Long userId
    ) {
            return ResponseEntity.ok(boardService.update(id, requestDto, userId));
    }

    // 삭제 - DELETE /api/boards/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestHeader("User-Id") Long userId
    ) {
            boardService.delete(id, userId);
            return ResponseEntity.noContent().build();
    }
}
