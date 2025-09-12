package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class ErrorKeywordLoginTypeException extends IllegalStateException {
    private final ErrorCode errorCode;

    public ErrorKeywordLoginTypeException() {
        super(ErrorCode.ERROR_KEYWORD_LOGIN_TYPE.getMessage());
        this.errorCode = ErrorCode.ERROR_KEYWORD_LOGIN_TYPE;
    }
}