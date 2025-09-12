package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class KakaoUserIdRequiredException extends IllegalStateException {
    private final ErrorCode errorCode;

    public KakaoUserIdRequiredException() {
        super(ErrorCode.KAKAO_USER_ID_REQUIRED.getMessage());
        this.errorCode = ErrorCode.KAKAO_USER_ID_REQUIRED;
    }
}