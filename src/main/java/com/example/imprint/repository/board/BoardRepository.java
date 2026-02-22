package com.example.imprint.repository.board;

import com.example.imprint.domain.board.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardRepository extends JpaRepository <BoardEntity, Long> {
    Optional<BoardEntity> findByName(String name);
}