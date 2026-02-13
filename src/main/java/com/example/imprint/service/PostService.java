package com.example.imprint.service;

import com.example.imprint.domain.BoardEntity;
import com.example.imprint.domain.post.PostRequestDto;
import com.example.imprint.domain.post.PostResponseDto;
import com.example.imprint.domain.user.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {
    Page<PostResponseDto> getPostList(BoardEntity board, Pageable pageable);
    PostResponseDto getPost(Long id);
    Long writePost(PostRequestDto dto, UserEntity user);
    void updatePost(Long id, PostRequestDto dto, UserEntity user);
    void deletePost(Long id, UserEntity user);
}