package com.springboot.config;

import com.springboot.auth.JwtTokenizer;
import com.springboot.auth.filter.JwtAuthenticationFilter;
import com.springboot.auth.filter.JwtVerificationFilter;
import com.springboot.auth.handler.MemberAuthenticationEntryPoint;
import com.springboot.auth.handler.MemberAuthenticationFailureHandler;
import com.springboot.auth.handler.MemberAuthenticationSuccessHandler;
import com.springboot.auth.utils.CustomAuthorityUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfiguration {

    //V2 : 추가
    private final JwtTokenizer jwtTokenizer;
    //추가 : 권한 설정
    private final CustomAuthorityUtils authorityUtils;

    public SecurityConfiguration(JwtTokenizer jwtTokenizer, CustomAuthorityUtils authorityUtils) {
        this.jwtTokenizer = jwtTokenizer;
        this.authorityUtils = authorityUtils;
    }

    //1. Spring Security 를 통한 보안 강화의 초기 (v1)
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                //'H2' 웹 콘솔이 내부적으로 <frame> 태그사용 -> 개발환경에서 사용하기 위해
                //frameOptions().sameOrigin() -> 호츌하면 동일 출처로부터 들어오는 request 만 페이지 렌더링 허용
                .headers().frameOptions().sameOrigin()
                .and()
                //csrf공격에 대한 Security 설저 비활성화 (로컬환경에서의 학습이니 CSRF 공격에 대한 설정 불필요)
                //설정 없으면 403에러 발생
                    //disable : 장애를 입히다.
                .csrf().disable()
                //corsConfigurationSource 의 이름으로 등록된 Bean 이용
                 //CORS 처리의 쉬운 방법은 CorsFilter 사용
                .cors(withDefaults()) //-> Bean 제공받아 CorsFilter 적용 가능
            //권한설정
                //세션 설정하지 않도록 설정
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                //폼로그인 방식 비활성화
                .formLogin().disable()
                //요청을 전송할 때마다, Id, PW 정보를 Header에 실어서 인증하는 방식
                    //현재 사용하지 않아 비활성화 -> UsernamePasswordAuthenticationFilter, BasicAuthenticationFilter 비활성화
                .httpBasic().disable()
            //예외처리 -> 사용하도록 등록!
                .exceptionHandling()
                .authenticationEntryPoint(new MemberAuthenticationEntryPoint())
                .and()
                //커스텀마이징된 Configuration을 추가할 수 있다.
                    //CustomConfigurer -> 씨큐리티의 configuration 을 개발자가 원하는 방향으로 정의할 수 있음
                .apply(new CustomFilterConfigurer())
                .and()
                //JWT 적용하기 전이니 모든 HTTP request 요청에 대한 접근 허용 설정
                 //이후 URL 별로 권한 적용 예정
                .authorizeHttpRequests(authorize -> authorize
                      //MemberController 를 통해 접근할 수 있는 리소스에 대한 접근 권한
                        //.permitAll() 회원가입은 모두가 할 수 있다.
                        .antMatchers(HttpMethod.POST, "/*/members").permitAll()
                        //.hasRole("USER") 회원 정보 수정은 해당 유저만 가능하다
                        .antMatchers(HttpMethod.PATCH, "/*/members/**").hasRole("USER")
                        //.hasRole("ADMIN") 회원전체 조회는 관리자만 가능하다
                        .antMatchers(HttpMethod.GET, "/*/members").hasRole("ADMIN")
                        //.hasAnyRole("USER", "ADMIN") 특정회원 조회는 해당사용자, 관리자만 가능하다.
                        //특정 회원 정보 조회 요청을 처리하는 MemberController의 getMember() 핸들러 메서드에 대한 접근 권한 부여 설정
                        .antMatchers(HttpMethod.GET, "/*/members/**").hasAnyRole("USER", "ADMIN")
                        //회원 삭제는 해당 사용자만 접근 가능하다.
                        .antMatchers(HttpMethod.DELETE,"/*/members/*").hasRole("USER")

                    //CoffeeController 를 통해 접근할 수 있는 리소스에 대한 접근 권한
                        //커피등록은 관리자만 접근 가능
                        .antMatchers(HttpMethod.POST, "/*/coffees").hasRole("ADMIN")
                        //커피 정보 수정은 관리자만 접근 가능
                        .antMatchers(HttpMethod.PATCH, "/*/coffees/**").hasRole("ADMIN")
                        //커피 전체 조회(정보제공)는 모두 접근 가능
                        .antMatchers(HttpMethod.GET, "/*/coffees").permitAll()
                        //특정 커피 조회(정보제공)는 모두 접근 가능
                        .antMatchers(HttpMethod.GET, "/*/coffees/**").permitAll()
                        //커피메뉴 삭제는 관리자만 접근 가능
                        .antMatchers(HttpMethod.DELETE,"/*/coffees/*").hasRole("ADMIN")

                    //OrderController 를 통해 접근할 수 있는 리소스에 대한 접근 권한
                        //주문은 회원만 가능
                        .antMatchers(HttpMethod.POST, "/*/orders").hasRole("USER")
                        //주문 수정은 회원과 관리자 접근 가능
                        .antMatchers(HttpMethod.PATCH, "/*/orders/**").hasAnyRole("USER", "ADMIN")
                        //모든 주문 조회는 관리자만 접근 가능
                        .antMatchers(HttpMethod.GET, "/*/orders").hasRole("ADMIN")
                        //특정 주문 조회는 관리자와 해당 회원만 접근 가능
                        .antMatchers(HttpMethod.GET, "/*/orders/**").hasAnyRole("USER", "ADMIN")
                        //주문 취소는 회원, 관리자만 접근 가능
                        //회원은 주문취소가 가능한 때 취소할 수 있어야한다, 관리자는 업장 사정으로 주문을 받지 못하는 상황일 때 부득이하게 취소
                        .antMatchers(HttpMethod.DELETE,"/*/orders/*").hasAnyRole("USER", "ADMIN")
                        //서버측으로 들어오는 모든 request에 대한 접근 허용
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    //구체적인 CORS 정책 설정
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //setAllowedOrigins() -> 모든 출처(Origin) 에 대해 스크립트 기반 Http 통신 허용 (운영서버 환경에서 요구사항에 맞게 변경 가능
        corsConfiguration.setAllowedOrigins(Arrays.asList("*"));
        //setAllowedMethods() -> 파라미터로 지정한 Http Method 에 대해 Http 통신 허용
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PATCH", "DELETE"));
        //CorsConfigurationSource 인터페이스의 구현 클래스
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        //모든 URL에 앞에서 구성한 CORS 정책(CorsConfiguration) 적용
        source.registerCorsConfiguration("/**", corsConfiguration);

        return source;
    }

    //Filter 구현 후 추가 -> Custom Configuration
    //JwtAuthentication를 등록하는 역할
        // <AbstractHttpConfigurer 상속하는 타입, HttpSecurityBuilder 상속하는 타입> 제네릭 타입으로 지정
    public class CustomFilterConfigurer extends AbstractHttpConfigurer<CustomFilterConfigurer, HttpSecurity> {

        @Override
        //Configuration을 커스터마이징
        public void configure(HttpSecurity builder) throws Exception {
//            getSharedObject(AuthenticationManager.class)를 통해 AuthenticationManager의 객체를 생성
            AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class);

            // JwtAuthenticationFilter를 생성하면서 사용될 AuthenticationManager와 JwtTokenizer를 DI
            JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager, jwtTokenizer);
           //디폴트 request URL : /login  ->  /v11/auth/login 으로 변경
            jwtAuthenticationFilter.setFilterProcessesUrl("/v11/auth/login");
        //v3 추가 : 로그인 인증 성공, 실패 핸들러 등록
            jwtAuthenticationFilter.setAuthenticationSuccessHandler(new MemberAuthenticationSuccessHandler());
            jwtAuthenticationFilter.setAuthenticationFailureHandler(new MemberAuthenticationFailureHandler());
        //추가 : 권한설정
            //JwtVerificationFilter에서 사용되는 객체들을 생성자로 DI
            JwtVerificationFilter jwtVerificationFilter = new JwtVerificationFilter(jwtTokenizer, authorityUtils);
            //JwtAuthenticationFilter를 Spring Security Filter Chain에 추가
            builder.addFilter(jwtAuthenticationFilter)
                //추가 : 권한설정
                    // JwtVerificationFilter를 JwtAuthenticationFilter 뒤에 추가
                    //로그인 인증에 성공한 . 발급받은 JWT가 클라이언트의 request header(Authorization 헤더)에 포함되어 있을 경우에만 동작
                    .addFilterAfter(jwtVerificationFilter, JwtAuthenticationFilter.class);

        } //-> 로그인 인증시  JWT 토큰이 응답으로 전달이 잘 되는지 확인
    }


}
