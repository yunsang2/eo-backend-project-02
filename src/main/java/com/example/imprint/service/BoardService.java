package com.example.imprint.service;

import com.example.imprint.domain.BoardEntity;
import com.example.imprint.domain.BoardRequestDto;
import com.example.imprint.domain.BoardResponseDto;
import com.example.imprint.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;

    // 게시판 생성
    @Transactional // 쓰기 작업은 별도 명시
    public BoardEntity save(BoardRequestDto requestDto) {
        BoardEntity board = requestDto.toEntity();
        return boardRepository.save(board);
    }

    // 전체 조회
    public List<BoardResponseDto> findAll() {
        return boardRepository.findAll().stream()
                .map(BoardResponseDto::new)
                .collect(Collectors.toList());
    }

    // 단건 조회
    public BoardResponseDto findById(Long id) {
        BoardEntity board = boardRepository.findById(id)
                  .orElseThrow(() -> new IllegalArgumentException("해당 게시판이 없습니다. id=" + id));
        return new BoardResponseDto(board);
    }

    // 수정
    @Transactional
    public Long update(Long id, BoardRequestDto requestDto) {
        BoardEntity board = boardRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("해당 게시판이 없습니다. id =" + id));
        board.updateName(requestDto.getName());
        return board.getId();
    }

    // 삭제
    @Transactional
    public void delete(Long id) {
        BoardEntity board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시판이 없습니다. id =" + id));
        boardRepository.delete(board);
    }
}
