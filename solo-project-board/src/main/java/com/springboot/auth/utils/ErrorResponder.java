package com.springboot.auth.utils;

import com.google.gson.Gson;
import com.springboot.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ErrorResponder {
    //원래 MemberAuthenticationFailureHandler 에 로직 작성되어 있었음
    public static void sendErrorResponse(HttpServletResponse response, HttpStatus status) throws IOException {
        //Error 정보가 담긴 객체를 JSON 문자열로 변환하는데 사용되는 Gson 라이브러리의 인스턴스를 생성
        Gson gson = new Gson();
        //ErrorResponse 객체를 생성, 상태코드 전달 (401, UNAUTHORIZED : 인증 실패할 경우 전달할 수 있는 HttpStatus)
        ErrorResponse errorResponse = ErrorResponse.of(HttpStatus.UNAUTHORIZED);
        //ErrorResponse Content Type -> application.json 임을 클라이언트에게 알려줄 수 있도록 HTTP Header 추가
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        //상태코드를 클라이언트 한테 전달
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        //Gson 을 이용해서 ErrorResponse 객체를 Json 포맷 문자열로 변환 후, 출력 스트림 생성
        response.getWriter().write(gson.toJson(errorResponse, ErrorResponse.class));
    }
}
