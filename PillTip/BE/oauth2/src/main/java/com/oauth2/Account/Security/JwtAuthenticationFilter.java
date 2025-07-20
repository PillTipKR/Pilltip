package com.oauth2.Account.Security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.Account.Entity.Account;
import com.oauth2.Account.Repository.AccountRepository;
import com.oauth2.Account.Dto.ApiResponse;
import com.oauth2.Account.Dto.AuthMessageConstants;
import com.oauth2.Account.Entity.AccountToken;
import com.oauth2.Account.Repository.AccountTokenRepository;
import com.oauth2.Account.Service.TokenService;
import com.oauth2.Util.Exception.CustomException.InvalidTokenException;
import com.oauth2.Util.Exception.CustomException.TokenExpiredException;
import com.oauth2.Util.Exception.CustomException.UserInfoNotFoundRetryLoginException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.BadCredentialsException;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final TokenService tokenService;
    private final AccountTokenRepository accountTokenRepository;
    private final ObjectMapper objectMapper;
    private final AccountRepository accountRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // JWT 토큰 검증이 필요 없는 경로들
        String requestURI = request.getRequestURI();
        if (requestURI.equals("/api/auth/signup") || 
            requestURI.equals("/api/auth/login") || 
            requestURI.equals("/api/auth/social-login") ||
            requestURI.equals("/api/auth/check-duplicate") ||
            requestURI.equals("/api/auth/refresh") ||
            requestURI.startsWith("/oauth2/") ||
            requestURI.startsWith("/api/questionnaire/public/") ||
            requestURI.startsWith("/questionnaire/public/") ||
            requestURI.startsWith("/api/friend/inviting") ||
            requestURI.startsWith("/invite.html") ||
            requestURI.startsWith("/api/questionnaire/qr-url/all")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            // 요청 헤더에서 JWT 토큰 추출
            String jwt = getJwtFromRequest(request);
    
            // JWT 토큰이 없는 경우 명확한 에러 메시지 반환
            if (jwt == null) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(objectMapper.writeValueAsString(
                    ApiResponse.error(AuthMessageConstants.TOKEN_NOT_PROVIDED, "no_token")
                ));
                return;
            }

            // JWT 토큰이 유효한 경우 사용자 ID를 추출
            try {
                // 토큰에서 사용자 ID 추출
                Long accountId = tokenService.getUserIdFromToken(jwt);

                // DB에서 토큰 정보 조회
                AccountToken accountToken = accountTokenRepository.findById(accountId)
                        .orElseThrow(InvalidTokenException::new);

                // 토큰 일치 여부 및 만료 시간 검증
                if (!accountToken.getAccessToken().equals(jwt)) {
                    throw new InvalidTokenException();
                }

                if (accountToken.getAccessTokenExpiry().isBefore(LocalDateTime.now())) {
                    throw new TokenExpiredException();
                }

                // 사용자 정보 조회
                Account account = accountRepository.findById(accountId)
                        .orElseThrow(UserInfoNotFoundRetryLoginException::new);
                
                // 사용자 정보를 기반으로 인증 토큰 생성
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(account, null, null);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 인증 토큰을 컨텍스트에 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (RuntimeException e) {
                SecurityContextHolder.clearContext();
                throw new BadCredentialsException(e.getMessage());
            }
            
            // 정상적인 경우에만 필터 체인 계속 실행
            filterChain.doFilter(request, response);
            
        } catch (BadCredentialsException ex) {
            SecurityContextHolder.clearContext();
            // 에러 응답 반환
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            
            String errorType = ex.getMessage().contains("만료") ? "expired" : "invalid";
            String errorMessage = ex.getMessage();
            
            // 더 구체적인 에러 메시지 제공
            if (errorMessage.contains("유효하지 않은 토큰")) {
                errorMessage = AuthMessageConstants.TOKEN_INVALID_RETRY_LOGIN;
            } else if (errorMessage.contains("토큰이 만료")) {
                errorMessage = AuthMessageConstants.TOKEN_EXPIRED_RETRY_LOGIN;
            } else if (errorMessage.contains("사용자를 찾을 수 없습니다")) {
                errorMessage = AuthMessageConstants.USER_INFO_NOT_FOUND_RETRY_LOGIN;
            }
            
            response.getWriter().write(objectMapper.writeValueAsString(
                ApiResponse.error(errorMessage, errorType)
            ));
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(
                ApiResponse.error(AuthMessageConstants.AUTHENTICATION_FAILED, "invalid")
            ));
        }
    }

    /*
     * 요청 헤더에서 JWT 토큰 추출
     * Authorization 헤더에서 Bearer 토큰을 추출하고, 토큰 값을 반환
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // Authorization 헤더에서 Bearer 토큰을 추출
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Bearer 토큰 값 반환
        }
        return null; // 토큰이 없는 경우 null 반환
    }
}
