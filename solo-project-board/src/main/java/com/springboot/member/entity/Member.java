package com.springboot.member.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.springboot.audit.Auditable;
import com.springboot.board.entity.Board;
import com.springboot.comment.entity.Comment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Member extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(nullable = false, unique = true, updatable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, length = 13)
    private String phone;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus memberStatus = MemberStatus.MEMBER_ACTIVE;

    //사용자 등록 시 사용자 권한 등록하기 위한 권한테이블 생성
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = new ArrayList<>();

    //양방향매핑 : member
//    @JsonManagedReference
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Board> boards = new ArrayList<>();

    //양방향매핑 :Member-Comment
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    public enum MemberStatus {
        MEMBER_ACTIVE(1, "활동중"),
        MEMBER_SLEEP(2, "휴면상태"),
        MEMBER_QUIT(3, "탈퇴상태");

        @Getter
        private int status;

        @Getter
        private String message;

        MemberStatus(int status, String message) {
            this.status = status;
            this.message = message;
        }
    }

    //Member-Board 영속성 전이
    public void setBoard(Board board) {
        //현재 객체(Member)가 갖고있는 List<Board>에도 새로운 board 추가
        boards.add(board);
        //board가 가지고 있는 멤버가 this(현재 객체 Member) 와 다르다면
        if(board.getMember() != this) {
            //board에 추가
            board.setMember(this);
        }
    }
    //Member-Comment 영속성 전이
    public void setComment(Comment comment) {
        //comment가 Member(this)의 memberId를 가지고있지 않으면 추가
        if(comment.getMember().getMemberId() != this.memberId) {
            comment.setMember(this);
        }
        comments.add(comment);
    }

}

