package com.springboot.auth.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
//로그인 인증 성공했을 때, 사용자 정보를 response로 전송하는 등의 추가 처리를 할수있는 핸들러
//인터페이스 구현
public class MemberAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
   //Authentication 객체에 사용자 정보를 얻은 후, HttpServletResponse 로 출력 스트림을 생성하여 response를 전송할 수 있다.
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        //인증 성공 후, 로그를 기록하거나 사용자 정보를 response로 전송하는 등의 추가 작업을 할 수 있다.
        log.info("# Authentication successfully!");
    }
}
