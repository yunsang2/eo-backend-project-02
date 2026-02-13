package com.example.imprint.repository;

import com.example.imprint.domain.BoardEntity;
import com.example.imprint.domain.post.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<PostEntity, Long> {

    Page<PostEntity> findByBoard(BoardEntity board, Pageable pageable);

    @Modifying
    @Query("update PostEntity p set p.views = p.views + 1 where p.id = :id")
    int updateViews(@Param("id") Long id);
}