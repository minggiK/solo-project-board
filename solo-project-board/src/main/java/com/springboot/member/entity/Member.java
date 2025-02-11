package com.springboot.member.entity;

import com.springboot.audit.Auditable;
import com.springboot.board.entity.Board;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Member extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(nullable = false, unique = true, updatable = false)
    private String email;

    @Column(nullable = false, length = 50)
    private String password;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, length = 13)
    private String phone;

    @Enumerated(value = EnumType.STRING)
    private MemberStatus memberStatus = MemberStatus.MEMBER_ACTIVE;

    //사용자 등록 시 사용자 권한 등록하기 위한 권한테이블 생성
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Board> boards = new ArrayList<>();

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

}

