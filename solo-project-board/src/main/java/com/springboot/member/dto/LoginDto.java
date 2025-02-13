package com.springboot.member.dto;

import lombok.Getter;

@Getter
public class LoginDto {
    //로그인 인증, 역직렬화를 위한 클래스
    private String email;
    private String password;
}
