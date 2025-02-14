package com.springboot.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;


public class CommentDto {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Post {
        //답변 내용은 필수 입력사항이다.
        @NotBlank(message = "글을 작성해 주세요.")
        private String content;

        private Long memberId;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Patch{

        private Long commentId;

        @NotBlank(message = "글을 작성해 주세요.")
        private String content;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {

        private Long commentId;
        private String content;
    }
}
