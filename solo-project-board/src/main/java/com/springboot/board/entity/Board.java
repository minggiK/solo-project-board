package com.springboot.board.entity;


import com.springboot.audit.Auditable;
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

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private BoardStatus boardStatus = BoardStatus.QUESTION_REGISTERED;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private BoardPublicStatus publicStatus = BoardPublicStatus.QUESTION_PUBLIC;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

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
