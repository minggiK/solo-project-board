package com.springboot.auth.filter;

import com.springboot.auth.JwtTokenizer;
import com.springboot.auth.utils.CustomAuthorityUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

//1. JWT 자격 검증 기능 구현
    //OncePerRequestFilter 확장해서 request 당 한번만 실행되는 Security Filter 구현할 수 있다.
        //JWT 검증은 request 당 한번만 실행되기 때문에 -> OncePerRequestFilter 적절
//클라이언트로부터 전달받은 JWT의 Claims를 얻는 과정에서 내부적으로 JWT에 대한 서명(Signature)을 검증
public class JwtVerificationFilter extends OncePerRequestFilter {
    //JWT 검증하고 Claims(토큰에 포함된 정보)를 얻을 때 사용
    private final JwtTokenizer jwtTokenizer;
    //JWT 검증에 성공하면 Authentication 객체에 채울 사용자의 권한을 생성
    private final CustomAuthorityUtils authorityUtils;

    public JwtVerificationFilter(JwtTokenizer jwtTokenizer, CustomAuthorityUtils authorityUtils) {
        this.jwtTokenizer = jwtTokenizer;
        this.authorityUtils = authorityUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
    //로직 추가 : 예외처리
        // try~catch 문으로 특정 예외 타입의 Exception이 catch 되면 해당 Exception을 HttpServletRequest의 애트리뷰트로 추가
        //예외가 발생하게 되면 SecurityContext에 클라이언트의 인증 정보(Authentication 객체)가 저장되지 않는다.
        try {
            Map<String, Object> claims = verifyJws(request);
            //Authentication 객체를 SecurityContext에 저장
            setAuthenticationToContext(claims);
        } catch (SignatureException se) {
            request.setAttribute("exception", se);

        } catch (ExpiredJwtException ee) {
            request.setAttribute("exception", ee);

        } catch (Exception e) {
            request.setAttribute("exception", e);
        }

        //JWT의 서명 검증에 성공하고, Security Context에 Authentication을 저장하 다음(Next) Secur면ty Filter를 호출
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        //Authorization header
        String authorization = request.getHeader("Authorization");
        //Authorization header의 값이 null, Authorization header의 값이 “Bearer”로 시작하지 않는다면 해당 Filter의 동작을 수행하지 않도록 정의
        //-> JWT가 Authorization header에 포함되지 않았다면 JWT 자격증명이 필요하지 않은 리소스에 대한 요청이라고 판단하고 다음(Next) Filter로 처리를 넘기는 것
        //실수로 JWT 포함 안해도 Authentication이 정상적으로 SecurityContext에 저장되지 않은 상태이기 때문에 다른 Security Filter를 거쳐 결국 Exception을 던짐
        return authorization == null || !authorization.startsWith("Bearer");

    }

    //JWT를 검증
    private Map<String, Object> verifyJws(HttpServletRequest request) {
       // request의 header에서 JWT를 얻고 있다. : 클라이언트가 response Header로 전달받은 JWT를 request header 에 추가해서 서버에 전송한 것
       // 변수명 jws : JSON Web Token Signed
        //replace() -> "Bearer " 제거
        String jws = request.getHeader("Authorization").replace("Bearer ", "");
        //JWT 서명(Signature) 검증을 위한 SecretKey 받음
        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
        //JWT 에서 claims를 파싱(파싱(parsing)은 특정 형식의 데이터나 문서를 분석하고 해석하는 과정)
        //JWT에서 Claims를 파싱 할 수 있다는 의미는 내부적으로 서명(Signature) 검증에 성공했다는 의미
            // getClaims에 담은 두개의 값이 정상적으로 파싱되면 서명 검증도 성공한다
        Map<String, Object> claims = jwtTokenizer.getClaims(jws, base64EncodedSecretKey).getBody();

        return claims;
    }

    //Authentication 객체를 SecurityContext 에 저장하기 위한 메서드
    private void setAuthenticationToContext(Map<String, Object> claims) {
        //JWT에서 파싱한 clamis 의 username 을 얻는다.
        String username = (String) claims.get("username");
        //JWT claims에서 얻은 권한 정보를 기반으로 List<GrantedAuthority> 생성
        List<GrantedAuthority> authorities = authorityUtils.createAuthorities((List) claims.get("roles"));
        //username, List<GrantedAuthority>를 가진 객체 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
        //SecurityContext에 Authentication 객체 저장
            //SecurityContext에 Authentication을 저장하게 되면 Spring Security의 세션 정책(Session Policy)에 따라서 세션을 생성하거나, 그렇지 않을 수도 있다.
            //JWT 환경에서는 세션 정책(Session Policy) 설정을 통해 세션 자체를 생성하지 않도록 설정
        SecurityContextHolder.getContext().setAuthentication(authentication);


    }


}
