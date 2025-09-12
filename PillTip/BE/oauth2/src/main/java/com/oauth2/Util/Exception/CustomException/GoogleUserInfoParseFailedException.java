package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class GoogleUserInfoParseFailedException extends IllegalStateException {
    private final ErrorCode errorCode;

    public GoogleUserInfoParseFailedException() {
        super(ErrorCode.GOOGLE_USER_INFO_PARSE_FAILED.getMessage());
        this.errorCode = ErrorCode.GOOGLE_USER_INFO_PARSE_FAILED;
    }
}