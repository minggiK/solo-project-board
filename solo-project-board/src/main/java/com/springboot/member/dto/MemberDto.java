package com.springboot.member.dto;

import com.springboot.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;


public class MemberDto {

    @AllArgsConstructor
    @Getter
    public static class Post{

    @Email
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String username;

    @NotBlank
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "숫자 11자리와 '-'를 포함하여 입력해주세요. 예시 010-1111-2222")
    private String phone;
    }

    @AllArgsConstructor
    @Getter
    public static class Patch{
        private Long memberId;

        @NotBlank
        private String password;

        @NotBlank
        private String username;

        @NotBlank
        @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "숫자 11자리와 '-'를 포함하여 입력해주세요. 예시 010-1111-2222")
        private String phone;

        public void setMemberId(long memberId) { this.memberId = memberId; }
    }

    @AllArgsConstructor
    @Getter
    public static class Response {
        private Long memberId;
        private String email;
//        private String password;
        private String username;
        private String phone;
        private Member.MemberStatus memberStatus;

    }
}
