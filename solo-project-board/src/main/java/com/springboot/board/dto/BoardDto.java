package com.springboot.board.dto;

import com.springboot.board.entity.Board;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotBlank;


public class BoardDto {
    @Getter
    @AllArgsConstructor
    public static class Post {
        @NotBlank(message = "제목을 입력해 주세요.")
        private String title;

        @NotBlank(message = "본문을 작성해 주세요.")
        private String text;



    }

    @Getter
    @AllArgsConstructor
    public static class Patch {

        private Long boardId;

        @NotBlank(message = "제목을 입력해 주세요.")
        private String title;

        @NotBlank(message = "본문을 작성해 주세요.")
        private String text;

    }

    @Getter
    @AllArgsConstructor
    public static class Response {
        private Long boardId;
        private String title;
        private String text;
        private Board.BoardStatus boardStatus;
        private Board.BoardPublicStatus publicStatus;
        //조회수
        private int checkView;
        //좋아요 수
        private int likeCount;
    }
}
