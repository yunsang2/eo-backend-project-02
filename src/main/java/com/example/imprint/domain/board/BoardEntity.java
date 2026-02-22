package com.example.imprint.domain.board;

import com.example.imprint.domain.BaseTimeEntity;
import com.example.imprint.domain.post.PostEntity;
import com.example.imprint.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "boards")
public class BoardEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;

    @ManyToMany
    @JoinTable(
            name = "board_managers",
            joinColumns = @JoinColumn(name = "board_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<UserEntity> managerList = new ArrayList<>();

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
    private List<PostEntity> postList = new ArrayList<>();

    public BoardEntity(String name) {
        this.name = name;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void addPost(PostEntity post) {
        this.postList.add(post);
        if (post.getBoard() != this) {
            post.updateBoard(this);
        }
    }

    public void addManager(UserEntity user) {
        if (!this.managerList.contains(user)) {
            this.managerList.add(user);
            user.getManagingBoardList().add(this);
        }
    }

    public void removeManager(UserEntity user) {
        if (this.managerList.contains(user)) {
            this.managerList.remove(user);
            user.getManagingBoardList().remove(this);
        }
    }
}
