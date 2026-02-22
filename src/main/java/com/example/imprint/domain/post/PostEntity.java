package com.example.imprint.domain.post;

import com.example.imprint.domain.BaseTimeEntity;
import com.example.imprint.domain.board.BoardEntity;
import com.example.imprint.domain.comment.CommentEntity;
import com.example.imprint.domain.user.UserEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "posts")
public class PostEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", referencedColumnName = "id")
    private BoardEntity board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private UserEntity writer;

    @NotBlank(message = "제목을 입력해주세요.")
    @Size(min = 2, max = 80, message = "제목은 2 ~ 80자여야 합니다.")
    @Column(name = "title", length = 80, nullable = false)
    private String title;

    @NotBlank(message = "본문을 입력해주세요.")
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<CommentEntity> commentList = new ArrayList<>();

    @Builder
    public PostEntity(BoardEntity board, UserEntity writer, String title, String content) {
        this.board = board;
        this.writer = writer;
        this.title = title;
        this.content = content;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateBoard(BoardEntity board) {
        this.board = board;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void addComment(CommentEntity comment) {
        this.commentList.add(comment);
        if (comment.getPost() != this) {
            comment.updatePost(this);
        }
    }
}