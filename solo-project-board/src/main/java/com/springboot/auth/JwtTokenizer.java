package com.springboot.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

//로그인 인증에 성공한 사용자에게 JWT 생성 및 발급 -> 요청이 들어올 떄마다 전달되는 JWT 검증
//Bean 등록
@Component
public class JwtTokenizer {

//JWT 생성에 필요한 필드  -> application.yml 에서 로드
    //JWT 생성 및 검증 시 사용도는 SecretKey 정보
    @Getter
    @Value("${jwt.key}")
    private String secretKey;

    //Access Token의 만료시간 정보
    @Getter
    @Value("${jwt.access-token-expiration-minutes}")
    private int accessTokenExpirationMinutes;

    //Refresh Token의 만료시간 정보
    @Getter
    @Value("${jwt.refresh-token-expiration-minutes}")
    private int refreshTokenExpirationMinutes;

    //Plain Text 형태인 Secret Key의 byte[]를 Base64 형식의 문자열로 인코딩
    public String encodeBase64SecretKey(String secretKey) {
        return Encoders.BASE64.encode(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // 인증된 사용자에게 JWT를 최초로 발급해 주기 위한 JWT 생성 메서드
    public String generateAccessToken(Map<String, Object> claims,
                                      String subject,
                                      Date expiration,
                                      String base64EncodedSecretKey) {
//        Base64 형식 Secret Key 문자열을 이용해 Key(java.security.Key) 객체 생성
        Key key = getKeyFromBase64EncodedKey(base64EncodedSecretKey);

        return Jwts.builder()
                .setClaims(claims) //인증된 사용자와 관련된 정보 추가
                .setSubject(subject) // JWT에 제목
                .setIssuedAt(Calendar.getInstance().getTime()) //JWT 발행일자 설정(Date 타입을 인자로 받음)
                .setExpiration(expiration) //JWT 만료일시 지정(DATE 타입을 인자로 받음)
                .signWith(key) // 서명을 위한 Key 객체를 설정
                .compact(); // JWT 생성하고 직렬화함
    }

    //Access 토큰이 만료되면, 새로 생성할 수 있도록 RefreshToken을 생성
        //RefreshToken 이 AccessToken을 새로 발급해주는 역할을 하는 토큰이기 때문에 별도의 claims를 추가하지 않아도된다.
    public String generateRefreshToken(String subject,
                                       Date expiration,
                                       String base64EncodedSecretKey) {
        Key key = getKeyFromBase64EncodedKey(base64EncodedSecretKey);

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(Calendar.getInstance().getTime())
                .setExpiration(expiration)
                .signWith(key)
                .compact();

    }

    public Jws<Claims> getClaims(String jws, String base64EncodedSecretKey) {
        Key key = getKeyFromBase64EncodedKey(base64EncodedSecretKey);

        //검증 메서드 (두개의 값이 같지 않을 때 오류발생)
        Jws<Claims> claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jws);
        return claims;
    }

    public void verifySignature(String jws, String base64EncodedSecretKey){
        Key key = getKeyFromBase64EncodedKey(base64EncodedSecretKey);

        Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jws);
    }

    //JWT 만료일시를 저장하기 위한 메서드 -> JWT 생성시 사용
    public Date getTokenExpiration(int expirationMunutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, expirationMunutes);
        Date expiration = calendar.getTime();

        return expiration;
    }

    //JWT 서명에 사용할 SecretKey 생성
    private Key getKeyFromBase64EncodedKey(String base64EncodedSecretKey) {
        //Base64 형식으로 인코딩된SecretKey를 디코딩한후, byte array 반환
        byte[] keyBytes = Decoders.BASE64.decode(base64EncodedSecretKey);
        //key byte array를 기반으로 적절한 HMAC 알고리즘을 적용한 key객체 생성
        Key key = Keys.hmacShaKeyFor(keyBytes);

        return key;
    }
}
