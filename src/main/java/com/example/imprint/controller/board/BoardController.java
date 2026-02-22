package com.example.imprint.controller.board;

import com.example.imprint.domain.ApiResponseDto;
import com.example.imprint.domain.board.BoardDto;
import com.example.imprint.domain.page.CriteriaDto;
import com.example.imprint.domain.page.PaginationDto;
import com.example.imprint.domain.user.UserResponseDto;
import com.example.imprint.service.board.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<BoardDto.IdResponse>> create(
            @RequestBody BoardDto.Create dto) {
        Long id = boardService.create(dto);

        return ResponseEntity.ok(
                ApiResponseDto.success(new BoardDto.IdResponse(id), "게시판을 성공적으로 생성하였습니다.")
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<BoardDto.PagedResponse>> findAll(
            CriteriaDto criteria) {
        Pageable pageable = PageRequest.of(
                criteria.getPage() - 1,
                criteria.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<BoardDto.Response> boardDtoPage = boardService.getBoardList(pageable);

        PaginationDto pagination = PaginationDto.of(pageable,
                boardDtoPage.getTotalElements(), boardDtoPage.getTotalPages());

        return ResponseEntity.ok(
                ApiResponseDto.success(
                        new BoardDto.PagedResponse(boardDtoPage.getContent(), pagination),
                        "게시판 목록을 성공적으로 조회하였습니다.")
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<BoardDto.Response>> findById(
            @PathVariable Long id) {
        BoardDto.Response board = boardService.get(id);

        return ResponseEntity.ok(
                ApiResponseDto.success(board, "게시판 정보를 조회했습니다.")
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<BoardDto.IdResponse>> update(
            @RequestBody BoardDto.Update dto,
            @PathVariable Long id) {
        Long updatedId = boardService.update(dto, id);

        return ResponseEntity.ok(
                ApiResponseDto.success(new BoardDto.IdResponse(updatedId), "게시판이 성공적으로 수정되었습니다.")
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> delete(
            @PathVariable Long id) {
        boardService.delete(id);

        return ResponseEntity.ok(
                ApiResponseDto.success("게시판이 삭제되었습니다.")
        );
    }

    @GetMapping({"/{id}/manager"})
    public ResponseEntity<ApiResponseDto<List<UserResponseDto>>> getManager(
            @PathVariable Long id) {
        List<UserResponseDto> boardList = boardService.getManagerList(id);

        return ResponseEntity.ok(
                ApiResponseDto.success(boardList, "게시판 매니저 리스트를 성공적으로 조회했습니다.")
        );
    }

    @PostMapping("/{id}/manager")
    public ResponseEntity<ApiResponseDto<Void>> addManager(
            @RequestBody BoardDto.ManagerRequest user,
            @PathVariable Long id) throws AccessDeniedException {
        boardService.addManager(id, user.id());

        return ResponseEntity.ok(
                ApiResponseDto.success("게시판에 매니저를 등록했습니다.")
        );
    }

    @DeleteMapping("/{id}/manager")
    public ResponseEntity<ApiResponseDto<Void>> removeManager(
            @RequestBody BoardDto.ManagerRequest user,
            @PathVariable Long id) throws AccessDeniedException {
        boardService.removeManager(id, user.id());

        return ResponseEntity.ok(
                ApiResponseDto.success("게시판에서 매니저를 제외했습니다.")
        );
    }
}