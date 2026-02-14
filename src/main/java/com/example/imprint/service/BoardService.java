package com.example.imprint.service;

import com.example.imprint.domain.BoardEntity;
import com.example.imprint.domain.BoardRequestDto;
import com.example.imprint.domain.BoardResponseDto;
import com.example.imprint.domain.user.UserEntity;
import com.example.imprint.repository.BoardRepository;
import com.example.imprint.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    // 게시판 생성 (유저 ID 받기)
    @Transactional
    public BoardResponseDto save(BoardRequestDto dto, Long userId) {
         UserEntity creator = userRepository.findById(userId)
                 .orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다. id=" + userId));

        // 2.게시판 생성 (유저 포함)
        BoardEntity board = dto.toEntity(creator);

        // 3. 저장
        BoardEntity saved = boardRepository.save(board);

        return new BoardResponseDto(saved);
    }

    // 전체 조회
    public List<BoardResponseDto> findAll() {
        return boardRepository.findAll().stream()
                .map(BoardResponseDto::new)
                .collect(Collectors.toList());
    }

    // 단건 조회
    public BoardResponseDto findByName(String name) {
        BoardEntity board = boardRepository.findByName(name)
                  .orElseThrow(() -> new IllegalArgumentException("해당 게시판이 없습니다. name=" + name));
        return new BoardResponseDto(board);
    }

    // 수정
    @Transactional
    public Long update(Long id, BoardRequestDto requestDto, Long userId) {
        BoardEntity board = boardRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("해당 게시판이 없습니다. id=" + id));

        if (!board.isCreator(userId)) {
            throw new IllegalArgumentException("게시판을 수정할 권한이 없습니다.");
        }

        board.update(requestDto.getName());
        return board.getId();
    }

    // 게시판 삭제
    @Transactional
    public void delete(Long id, Long userId) {
        BoardEntity board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시판이 없습니다. id =" + id));
        if (!board.isCreator(userId)) {
            throw new IllegalArgumentException("게시판을 삭제할 권한이 없습니다.");
        }

        boardRepository.delete(board);
    }

}
