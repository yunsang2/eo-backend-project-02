package com.example.imprint.domain.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostRequestDto {

    @NotBlank(message = "제목과 내용은 반드시 입력되어야 합니다.")
    @Size(min = 1, max = 80, message = "제목 글자수는 1자 이상 80자 이하")
    private String title;

    @NotBlank(message = "제목과 내용은 반드시 입력되어야 합니다.")
    private String content;

    private Long boardId;
}