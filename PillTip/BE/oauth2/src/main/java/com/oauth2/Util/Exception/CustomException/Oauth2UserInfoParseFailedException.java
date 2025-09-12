package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class Oauth2UserInfoParseFailedException extends IllegalStateException {
    private final ErrorCode errorCode;

    public Oauth2UserInfoParseFailedException() {
        super(ErrorCode.OAUTH2_USER_INFO_PARSE_FAILED.getMessage());
        this.errorCode = ErrorCode.OAUTH2_USER_INFO_PARSE_FAILED;
    }
}