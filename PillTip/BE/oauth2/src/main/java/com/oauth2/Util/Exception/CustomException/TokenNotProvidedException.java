package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class TokenNotProvidedException extends IllegalStateException {
    private final ErrorCode errorCode;

    public TokenNotProvidedException() {
        super(ErrorCode.TOKEN_NOT_PROVIDED.getMessage());
        this.errorCode = ErrorCode.TOKEN_NOT_PROVIDED;
    }
}