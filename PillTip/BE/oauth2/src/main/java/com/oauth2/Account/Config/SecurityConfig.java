// author : mireutale
// description : 보안 설정

package com.oauth2.Account.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.Account.Dto.ApiResponse;
import com.oauth2.Account.Dto.AuthMessageConstants;
import com.oauth2.Account.Security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpStatus;

@Configuration // 스프링 설정 클래스임을 명시
@EnableWebSecurity // 웹 보안 설정 활성화
@EnableMethodSecurity//컨트롤러 레벨 보안 설정 활성화
@RequiredArgsConstructor // 생성자 주입 설정
public class SecurityConfig {
    // 커스텀 OAuth2 사용자 서비스 및 JWT 인증 필터를 주입받음
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    // 스프링 시큐리티 필터 체인을 구성하는 메서드
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // csrf 공격 방어 비활성화, REST API는 상태를 저장하지 않으므로
            .csrf(AbstractHttpConfigurer::disable)
            // 세션 관리 정책 설정, 세션을 사용하지 않으므로 STATELESS로 설정(jwt 사용)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 인증 없이 접근 가능한 경로 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/error", "/oauth2/**", "/login/**",
                                "/css/**", "/js/**", "/images/**",
                                "/static/**", "/webjars/**", "/favicon.ico",
                                "/api/auth/signup", "/api/auth/login", "/api/auth/check-duplicate",
                                "/api/auth/refresh", "/api/auth/social-login", "/api/questionnaire/public/**",
                                "/questionnaire/public/**",
                                "/profile/**", "/api/friend/inviting/**", "/invite.html",
                                "/api/questionnaire/qr-url/all"
                        ).permitAll()
                        // 나머지 경로는 인증 필요
                        .anyRequest().authenticated()
            )
            // 커스텀 인증 실패 핸들러 설정
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");

                    String errorMessage = AuthMessageConstants.TOKEN_NOT_PROVIDED;
                    String errorType = "no_token";

                    response.getWriter().write(objectMapper.writeValueAsString(
                        ApiResponse.error(errorMessage, errorType)
                    ));
                })
            )
            // JWT 인증 필터를 사용자 정보 엔드포인트 이전에 추가
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
