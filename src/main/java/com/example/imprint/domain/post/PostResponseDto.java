package com.example.imprint.domain.post;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class PostResponseDto {

    private final Long id;
    private final String title;
    private final String content;
    private final String nickname;
    private final int views;
    private final int recommend;
    //private final int commentCount;
    //private final boolean hasFile;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public PostResponseDto(PostEntity entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.content = entity.getContent();
        this.nickname = entity.getWriter().getNickname();
        this.views = entity.getViews();
        this.recommend = entity.getRecommend();
        //this.commentCount = entity.getComments().size();
        //this.hasFile = !entity.getFiles().isEmpty();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
    }
}