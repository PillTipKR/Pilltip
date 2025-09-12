package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class TokenNotFoundException extends IllegalStateException {
    private final ErrorCode errorCode;

    public TokenNotFoundException() {
        super(ErrorCode.TOKEN_NOT_FOUND.getMessage());
        this.errorCode = ErrorCode.TOKEN_NOT_FOUND;
    }
}