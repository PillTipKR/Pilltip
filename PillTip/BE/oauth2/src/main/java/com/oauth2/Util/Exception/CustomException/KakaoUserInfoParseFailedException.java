package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class KakaoUserInfoParseFailedException extends IllegalStateException {
    private final ErrorCode errorCode;

    public KakaoUserInfoParseFailedException() {
        super(ErrorCode.KAKAO_USER_INFO_PARSE_FAILED.getMessage());
        this.errorCode = ErrorCode.KAKAO_USER_INFO_PARSE_FAILED;
    }
}