package com.springboot.auth.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomAuthorityUtils {
    //@Value("${프로퍼티 경로}") : application.yml 에 추가한 프로퍼티 가져오는 표현식
    @Value("${mail.address.admin}")
    //application,yml 에 미리 정의한 관리자 권한을 설정한 이메일 주소를 불러옴 (admin@gmail.com)
    private String adminMalAddress;
    //관리자용 권한목록을 List<GrantedAuthority> 객체로 미리 생성 (Spring Security의 AuthorityUtils 기능) )
     //"ROLE_" + role
    private final List<GrantedAuthority> ADMIN_ROLES = AuthorityUtils.createAuthorityList("ROLE_USER", "ROLE_ADMIN");
    //일반 회원 사용 권한목록 객체 생성
    private final List<GrantedAuthority> USER_ROLES = AuthorityUtils.createAuthorityList("ROLE_USER");

    private final List<String> ADMIN_ROLES_STRING = List.of("ADMIN", "USER");
    private final List<String> USER_ROLES_STRING = List.of("USER");

    //DB 저장용
    public List<String> createRoles(String email) {
        if(email.equals(adminMalAddress)) {
            return ADMIN_ROLES_STRING;
        }
        return USER_ROLES_STRING;
    }
    //파라미터로 전달받은 email 이 yml에 지정한 관리자 이메일(admin@gmail.com)과 같다면 -> admin_roles 리턴
    //메모리 상의 Role을 기반으로 권한 정보 생성
    public List<GrantedAuthority> createAuthorities(String email) {
        if(email.equals(adminMalAddress)) {
            return ADMIN_ROLES;
        }
        return USER_ROLES;
    }

   //DB에 저장된 Role을 기반으로 권한 정보 생성
    public List<GrantedAuthority> createAuthorities(List<String> roles) {
        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
        return authorities;
    }


}
