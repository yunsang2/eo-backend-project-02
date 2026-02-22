package com.example.imprint.domain.comment;

import com.example.imprint.domain.BaseTimeEntity;
import com.example.imprint.domain.post.PostEntity;
import com.example.imprint.domain.user.UserEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "comments")
public class CommentEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", referencedColumnName = "id")
    private PostEntity post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private UserEntity writer;

    @NotBlank(message = "본문을 입력해주세요.")
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Builder
    public CommentEntity(PostEntity post, UserEntity writer, String content) {
        this.post = post;
        this.writer = writer;
        this.content = content;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updatePost(PostEntity post) {
        this.post = post;
    }
}
