package com.springboot.board.dto;

import com.springboot.board.entity.Board;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;


public class BoardDto {
    @Getter
    @AllArgsConstructor
    @Setter
    @NoArgsConstructor
    public static class Post {
        //1.서비스계층에서 createBoard : memberStatus가 Active 일 때만 글 작성이 가능해야한다.
        //2.board.getMember().getMemberId() 일때, getMember 에 null 이 들어옴
        //3.memberId를 요청에서 받아올 수 있도록 Dto에 추가 작성하고 mapper에 target, source 로 지정
        private Long memberId;

        @NotBlank(message = "제목을 입력해 주세요.")
        private String title;

        @NotBlank(message = "본문을 작성해 주세요.")
        private String content;

        private Board.BoardPublicStatus publicStatus;

    }

    @Getter
    @AllArgsConstructor
    public static class Patch {

        private Long boardId;

        @NotBlank(message = "제목을 입력해 주세요.")
        private String title;

        @NotBlank(message = "본문을 작성해 주세요.")
        private String content;

        private Board.BoardPublicStatus publicStatus;

    }

    @Getter
    @AllArgsConstructor
    public static class Response {
        private Long boardId;
        private String title;
        private String content;
        private Board.BoardStatus boardStatus;
        private Board.BoardPublicStatus publicStatus;
        //조회수
        private int checkView;
        //좋아요 수
        private int likeCount;
    }
}
