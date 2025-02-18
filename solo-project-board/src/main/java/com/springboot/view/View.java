package com.springboot.check;

import com.springboot.board.entity.Board;
import com.springboot.member.entity.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Entity
@Getter
public class CheckView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long checkViewId;

    @Column(nullable = false)
    private int checkViewCount;

    //board는 여러개의 조회수를 가질 수 있다.
    @ManyToOne
    @JoinColumn(name = "board_id")
    private Board board;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;
}
