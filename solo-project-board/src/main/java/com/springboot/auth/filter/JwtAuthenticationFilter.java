package com.springboot.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.auth.JwtTokenizer;
import com.springboot.member.dto.LoginDto;
import com.springboot.member.entity.Member;
import lombok.SneakyThrows;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//로그인 인증을 처리하는 엔트리포인트 역할을 하는 filter
    // UsernamePasswordAuthenticationFilter를 상속 받음
        // 폼로그인 방식에서 사용하는 디폴트 Secret Filter -> 폼로그인이 아니라도 Id, Pw 기반의 인증처리를 위해 확장하여 구현 가능
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

 //DI 받음
    //로그인 인증정보(Id, PW)를 전달받아 UserDetailsService 와 인터랙션 한뒤 인증 여부 판단
    private final AuthenticationManager authenticationManager;
    //클라이언트가 인증 성공 시 JWT 생성 및 발급
    private final JwtTokenizer jwtTokenizer;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtTokenizer jwtTokenizer) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenizer = jwtTokenizer;
    }

    //Throws, try-catch 통해 예외처리를 해줘야하는 경우에 명시적인 예외처리를 생략할 수 있다.
    @SneakyThrows
    @Override
    //내부에서 인증을 시도하는 로직 구현
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        //클라이언트에서 전송한 Id, PW 를 DTO 클래스로 역직렬화하기 위해 인스턴스 생성
        ObjectMapper objectMapper = new ObjectMapper();
        //ServletInputStream을 LoginDto 클래스의 객체로 역직렬화 한다.
        LoginDto loginDto = objectMapper.readValue(request.getInputStream(), LoginDto.class);
        //Id, pw 정보를 포함한 UsernamePasswordAuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());
        //UsernamePasswordAuthenticationToken 을 AuthenticationManager 로 전달하여 인증 처리를 위임
        return authenticationManager.authenticate(authenticationToken);
    }

    //클라이언트의 인증 정보를 이용해 인증에 성공할 경우 호출
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
       //Member 엔티티 클래스의 객체 생성
        //AuthenticationManager 내부에서 인증에 성공하면 인증된 Authentication 객체가 생성되면 principal 필드에 Member가 할당됨
        Member member = (Member) authResult.getPrincipal();
        //Access Token 생성
        String accessToken = delegateAccessToken(member);
        //Refresh Token 생성
        String refreshToken = delegateRefreshToken(member);
        //AccessToken 추가 -> 클라이언트가 백엔드 애플리케이션 측에 요청을 보낼 때마다 requestHeader에 추가해서, 클라이언트 자격을 증명하는데 사용
        response.setHeader("Authorization", "Bearer " + accessToken);
        //RefreshToken 추가
        //RefreshToken은 AccessToken 만료되면 클라이언트 측이 Access Token을 새로 발급받기 위해 클라이언트에게 추가적으로 제공됨
        response.setHeader("Refresh", refreshToken );

        //핸들러 구현 매서드 호출
        //로그인 인증에 실패하면 onAuthenticationFailure() 메서드가 알아서 호출된다.
        this.getSuccessHandler().onAuthenticationSuccess(request, response, authResult);

    }

    //AccessToken 생성하는 구체적인 로직
    private String delegateAccessToken(Member member) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", member.getEmail());
        claims.put("roles", member.getRoles());

        String subject = member.getEmail();
        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getAccessTokenExpirationMinutes());

        //plain text 향태인 SecretKey의 byte[]를 Base64형식의 문자열로 인코딩한다.
            //plain Text 자체를 SecretKey 로 사용하는 것을 권장하지 않고 있다.
        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
        //인증된 사용자에게 JWT 를 최초로 발급해 주기 위한 JWT 생성 메서드
        String accessToken = jwtTokenizer.generateAccessToken(claims, subject, expiration, base64EncodedSecretKey);

        return accessToken;
    }
    //RefreshToken 을 생성하는 로직
         //RefreshToken 이 AccessToken을 새로 발급해주는 역할을 하는 토큰이기 때문에 별도의 claims를 추가하지 않아도된다.
    private String delegateRefreshToken(Member member) {
        String subject = member.getEmail();
        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getRefreshTokenExpirationMinutes());

        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
        String refreshToken = jwtTokenizer.generateRefreshToken(subject, expiration, base64EncodedSecretKey);

        return refreshToken;
    }

} // -> Spring Security Filter Chain 에 추가해서 인증을 처리하도록 서렂 ㅇ
