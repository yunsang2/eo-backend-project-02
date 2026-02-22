package com.example.imprint.repository.post;

import com.example.imprint.domain.post.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    Page<PostEntity> findByBoardId(Long boardId, Pageable pageable);

    // 하루에 작성된 게시글 수
    long countByCreatedAtAfter(LocalDateTime startOfDay);

    // 👇 통합 검색 쿼리 (제목 OR 내용 OR 작성자 닉네임)
    @Query("SELECT p FROM PostEntity p JOIN p.writer w " +
            "WHERE p.title LIKE %:keyword% " +
            "OR p.content LIKE %:keyword% " +
            "OR w.nickname LIKE %:keyword% " +
            "ORDER BY p.createdAt DESC")
    Page<PostEntity> searchAllByKeyword(@Param("keyword") String keyword, Pageable pageable);
}