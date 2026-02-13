package com.example.imprint.controller;

import com.example.imprint.domain.BoardEntity;
import com.example.imprint.domain.BoardRequestDto;
import com.example.imprint.domain.BoardResponseDto;
import com.example.imprint.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController //REST API 컨트롤러
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    // 게시판 생성 - POST /api/boards
    @PostMapping
    public ResponseEntity<Long> save(@RequestBody BoardRequestDto requestDto) {
        BoardEntity board = boardService.save(requestDto);
        return ResponseEntity.ok(board.getId());
    }

    // 전체 조회 - Get /api/boards
    @GetMapping
    public ResponseEntity<List<BoardResponseDto>> findAll() {
        List<BoardResponseDto> boards = boardService.findAll();
        return ResponseEntity.ok(boards);
    }

    // 단건 조회 - GET /api/boards/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Long> findById(@PathVariable Long id) {
        BoardResponseDto board = boardService.findById(id);
        return ResponseEntity.ok(board.getId());
    }

    // 수정 - PUT /api/boards/{id}   ----- 패치방식으로 바꿀 예정
    @PutMapping("/{id}")
    public ResponseEntity<Long> update(@PathVariable Long id,
                                @RequestBody BoardRequestDto requestDto) {
        Long updatedId = boardService.update(id, requestDto);
        return ResponseEntity.ok(updatedId);
    }

    // 삭제 - DELETE /api/boards/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boardService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
