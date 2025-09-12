package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class TokenRequiredForSocialException extends IllegalStateException {
    private final ErrorCode errorCode;

    public TokenRequiredForSocialException() {
        super(ErrorCode.TOKEN_REQUIRED_FOR_SOCIAL.getMessage());
        this.errorCode = ErrorCode.TOKEN_REQUIRED_FOR_SOCIAL;
    }
}