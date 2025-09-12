package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class ErrorKeywordNicknameException extends IllegalStateException {
    private final ErrorCode errorCode;

    public ErrorKeywordNicknameException() {
        super(ErrorCode.ERROR_KEYWORD_NICKNAME.getMessage());
        this.errorCode = ErrorCode.ERROR_KEYWORD_NICKNAME;
    }
}