package com.springboot.auth.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.springboot.auth.utils.ErrorResponder.sendErrorResponse;

@Slf4j
//로그인 인증 실패에 대한 처리
//인터페이스 구현
public class MemberAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        //인증 실패시, 에러 로그를 기록하거나 error response를 전송할 수있다.
        log.error("#Authentication failed: {}", exception.getMessage());
        //error정보를 담음
        sendErrorResponse(response, HttpStatus.UNAUTHORIZED);
    }

//    private void sendErrorResponse(HttpServletResponse response) throws IOException{
//        //Error 정보가 담긴 객체를 JSON 문자열로 변환하는데 사용되는 Gson 라이브러리의 인스턴스를 생성
//        Gson gson = new Gson();
//        //ErrorResponse 객체를 생성, 상태코드 전달 (401, UNAUTHORIZED : 인증 실패할 경우 전달할 수 있는 HttpStatus)
//        ErrorResponse errorResponse = ErrorResponse.of(HttpStatus.UNAUTHORIZED);
//        //ErrorResponse Content Type -> application.json 임을 클라이언트에게 알려줄 수 있도록 HTTP Header 추가
//        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//        //상태코드를 클라이언트 한테 전달
//        response.setStatus(HttpStatus.UNAUTHORIZED.value());
//        //Gson 을 이용해서 ErrorResponse 객체를 Json 포맷 문자열로 변환 후, 출력 스트림 생성
//        response.getWriter().write(gson.toJson(errorResponse, ErrorResponse.class));
//    } //->예외페이지  ErrorResponse
}
