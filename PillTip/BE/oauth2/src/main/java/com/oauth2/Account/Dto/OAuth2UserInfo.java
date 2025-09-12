// author : mireutale
// description : OAuth2 사용자 정보
package com.oauth2.Account.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2UserInfo {
    private String socialId;
    private String email;
    private String name;
    private String profileImage;
    private String provider;
}
