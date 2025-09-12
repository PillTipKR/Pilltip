package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class ErrorKeywordUserIdException extends IllegalStateException {
    private final ErrorCode errorCode;

    public ErrorKeywordUserIdException() {
        super(ErrorCode.ERROR_KEYWORD_USER_ID.getMessage());
        this.errorCode = ErrorCode.ERROR_KEYWORD_USER_ID;
    }
}