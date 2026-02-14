package com.example.imprint.repository;

import com.example.imprint.domain.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardRepository extends JpaRepository <BoardEntity, Long> {
    BoardEntity save(BoardEntity Entity);
        // JpaRepository가 기본 CROUD 제공
        // save(), findById(), findAll(), delete() 등
    Optional<BoardEntity> findByName(String name);
}