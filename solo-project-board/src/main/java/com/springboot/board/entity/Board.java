package com.springboot.board.entity;


import com.springboot.audit.Auditable;
import com.springboot.comment.entity.Comment;
import com.springboot.member.entity.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class Board extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private String text;

    //게시글 상태
    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private BoardStatus boardStatus = BoardStatus.QUESTION_REGISTERED;

    //게시글 공개여부
    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private BoardPublicStatus publicStatus = BoardPublicStatus.QUESTION_PUBLIC;

    //외래키 : member는 여러 Board(질문)을 가질 수 있다.
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    //comment(답글) 과 1대1관계
    @OneToOne
    @JoinColumn(name = "comment_id")
    private Comment comment;


    //영속성 전이
    //member가 가지지고있는 Board(this)가 아니라면, 추가(setBoards)
    public void setMember(Member member) {
        this.member = member;
        if(!member.getBoards().contains(this)) {
            member.setBoard(this);
        }
    }


    //Board(질문 글) 상태
    public enum BoardStatus {
        QUESTION_REGISTERED("질문 등룍 상태"),
        QUESTION_ANSWERED("답변 완료 상태"),
        QUESTION_DELETE("질문 삭제 상태"),
        QUESTION_DEACTIVED("질문 비활성화 상태: 회원 탈퇴 시, 질문 비활성화");

        @Getter
        private String message;

        BoardStatus(String message) {
            this.message = message;
        }
    }

    //Board 상태 변경 setter
    public void setBoardStatus(BoardStatus boardStatus) {
        this.boardStatus = boardStatus;
    }

    //Board의 공개여부
    public enum BoardPublicStatus {
        QUESTION_PUBLIC("공개"),
        QUESTION_SECRET("비공개");

        @Getter
        private String message;

        BoardPublicStatus(String message) {
            this.message = message;
        }
    }
}
