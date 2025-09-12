// author : mireutale
// description : 사용자 세부 정보 서비스
package com.oauth2.Account.Security;

import com.oauth2.Account.Entity.Account;
import com.oauth2.Account.Repository.AccountRepository;
import com.oauth2.Util.Exception.CustomException.UserInfoNotFoundRetryLoginException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final AccountRepository accountRepository;

    /*
     * 스프링 시큐리티가 로그인 요청 시 호출하는 메서드
     * 사용자 ID를 기반으로 사용자 정보를 조회하고, 인증에 필요한 UserDetails 객체를 반환함
     */
    @Override
    public UserDetails loadUserByUsername(String accountId) throws UsernameNotFoundException {
        // 사용자 ID를 기반으로 사용자 정보를 조회
        Account account = accountRepository.findById(Long.parseLong(accountId))
                .orElseThrow(UserInfoNotFoundRetryLoginException::new);

        // 사용자 정보가 없으면 예외 발생
        if (account == null) {
            throw new UserInfoNotFoundRetryLoginException();
        }

        // 사용자 정보를 기반으로 UserDetails 객체 생성
        return new org.springframework.security.core.userdetails.User(
                String.valueOf(account.getId()), // 사용자 ID
                account.getPasswordHash() != null ? account.getPasswordHash() : "", // 사용자 비밀번호
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")) // 사용자 권한
        );
    }
}
