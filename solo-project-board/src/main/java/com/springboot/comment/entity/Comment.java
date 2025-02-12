package com.springboot.comment.entity;

import com.springboot.audit.Auditable;
import com.springboot.member.entity.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class Comment extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    //답글 = 댓글 : title을 자동으로 생성한다고 해도 굳이 table로 가지고 있을 필요 없음
//    @Column(nullable = false)
//    private String title;

    //한 게시물(질문)에 한가지 답변만 등록 가능
    @Column(nullable = false)
    private String text;

    //member 중 관리자 권한이 있는 자만 작성 가능
    //관리자는 1개의 게시글에 1개의 답변을 달아줄 수 있다
    //여러 게시글에 답변을 달아줄수 있음
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    //Comment-Member 영속성전이
    public void setMember(Member member) {
        //Comment가 가지고있는 필드 member와 param으로 들어온 member가 같다고 설정
        this.member = member;
        //param으로 들어온 member가 Comment(this)를 가지고 있지 않다면
        if(!member.getComments().contains(this)){
            //추가
            member.setComment(this);
        }
    }
}
