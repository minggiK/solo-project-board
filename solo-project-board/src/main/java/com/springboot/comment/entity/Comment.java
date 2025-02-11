package com.springboot.comment.entity;

import com.springboot.audit.Auditable;
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

    @Column(nullable = false)
    private String title;

    //한 게시물(질문)에 한가지 답변만 등록 가능
    @Column(nullable = false)
    private String content;



}
