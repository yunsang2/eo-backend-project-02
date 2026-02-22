package com.example.imprint.repository.post;

import com.example.imprint.domain.post.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    Page<PostEntity> findByBoardId(Long boardId, Pageable pageable);

    // 하루에 작성된 게시글 수
    long countByCreatedAtAfter(LocalDateTime startOfDay);
}