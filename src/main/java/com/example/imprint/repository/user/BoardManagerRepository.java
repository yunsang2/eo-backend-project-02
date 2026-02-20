package com.example.imprint.repository.user;

import com.example.imprint.domain.user.BoardManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardManagerRepository extends JpaRepository<BoardManager, Long> {

    // 특정 유저가 관리하는 모든 게시판 리스트 조회 (유저정보 페이지)
    @Query("SELECT bm FROM BoardManager bm JOIN FETCH bm.board WHERE bm.user.id = :userId")
    List<BoardManager> findAllByUserIdWithBoard(@Param("userId") Long userId);

    // 특정 게시판을 관리하는 모든 매니저 리스트 조회 (게시판관리 페이지)
    @Query("SELECT bm FROM BoardManager bm JOIN FETCH bm.user WHERE bm.board.id = :boardId")
    List<BoardManager> findAllByBoardIdWithUser(@Param("boardId") Long boardId);

    // 해당 유저가 해당 게시판의 매니저인지 확인
    boolean existsByBoardIdAndUserId(Long boardId, Long userId);

    // 특정 게시판+유저 조합으로 찾기
    Optional<BoardManager> findByBoardIdAndUserId(Long boardId, Long userId);

    // 특정 유저가 관리하는 모든 내역 찾기
    List<BoardManager> findAllByUserId(Long userId);
}