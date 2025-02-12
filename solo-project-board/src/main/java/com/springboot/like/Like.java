package com.springboot.like;

import com.springboot.board.entity.Board;
import com.springboot.member.entity.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.mapstruct.ap.internal.model.GeneratedType;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "Love")
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long likeId;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "board_id")
    private Board board;
}
