package com.example.imprint.service.board;

import com.example.imprint.domain.board.BoardDto;
import com.example.imprint.domain.user.UserResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface BoardService {
    Long create(BoardDto.Create requestDto);
    Long update(BoardDto.Update requestDto, Long boardId);
    void delete(Long boardId);
    BoardDto.Response get(Long boardId);
    List<BoardDto.Response> getBoardList();
    Page<BoardDto.Response> getBoardList(Pageable pageable);
    List<UserResponseDto> getManagerList(Long boardId);
    void addManager(Long boardId, Long userId) throws AccessDeniedException;
    void removeManager(Long boardId, Long userId) throws AccessDeniedException;
}
