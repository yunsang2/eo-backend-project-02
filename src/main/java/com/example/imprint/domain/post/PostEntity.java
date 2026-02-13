package com.example.imprint.domain.post;

import com.example.imprint.domain.BaseTimeEntity;
import com.example.imprint.domain.BoardEntity;
import com.example.imprint.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private BoardEntity board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity writer;

    @Column(nullable = false, length = 80)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ColumnDefault("0")
    private int views;

    @ColumnDefault("0")
    private int recommend;

    /*
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentEntity> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileEntity> files = new ArrayList<>();
     */

    @Builder
    public PostEntity(BoardEntity board, UserEntity writer, String title, String content) {
        this.board = board;
        this.writer = writer;
        this.title = title;
        this.content = content;
        this.views = 0;
        this.recommend = 0;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void incrementViews() {
        this.views++;
    }
}