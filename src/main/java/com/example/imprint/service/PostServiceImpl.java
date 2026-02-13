package com.example.imprint.service;

import com.example.imprint.domain.BoardEntity;
import com.example.imprint.domain.post.PostEntity;
import com.example.imprint.repository.BoardRepository;
import com.example.imprint.repository.PostRepository;
import com.example.imprint.domain.user.UserEntity;
import com.example.imprint.domain.user.UserRole;
import com.example.imprint.domain.post.PostRequestDto;
import com.example.imprint.domain.post.PostResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final BoardRepository boardRepository;

    @Override
    public Page<PostResponseDto> getPostList(BoardEntity board, Pageable pageable) {
        return postRepository.findByBoard(board, pageable)
                .map(PostResponseDto::new);
    }

    @Override
    @Transactional
    public PostResponseDto getPost(Long id) {
        int updatedCount = postRepository.updateViews(id);
        if (updatedCount == 0) {
            throw new IllegalArgumentException("존재하지 않거나 삭제된 게시물입니다.");
        }

        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 게시물입니다."));
        return new PostResponseDto(post);
    }

    @Override
    @Transactional
    public Long writePost(PostRequestDto dto, UserEntity user) {
        BoardEntity board = boardRepository.findById(dto.getBoardId())
                .orElseThrow(() -> new IllegalArgumentException("게시판 없음"));

        PostEntity post = PostEntity.builder()
        .title(dto.getTitle())
        .content(dto.getContent())
        .writer(user)
        .board(board)
        .build();

        return postRepository.save(post).getId();
    }

    @Override
    @Transactional
    public void updatePost(Long id, PostRequestDto dto, UserEntity user) {
        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 게시물입니다."));

        if (!post.getWriter().getId().equals(user.getId())) {
            throw new IllegalStateException("수정 권한이 없습니다.");
        }

        post.update(dto.getTitle(), dto.getContent());
    }

    @Override
    @Transactional
    public void deletePost(Long id, UserEntity user) {
        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 게시물입니다."));

        boolean isAdmin = user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.MANAGER;
        boolean isWriter = post.getWriter().getId().equals(user.getId());

        if (!isAdmin && !isWriter) {
            throw new IllegalStateException("삭제 권한이 없습니다.");
        }

        postRepository.delete(post);
    }
}