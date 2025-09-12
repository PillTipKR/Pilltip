package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class DuplicateLoginIdException extends IllegalStateException {
    private final ErrorCode errorCode;

    public DuplicateLoginIdException() {
        super("이미 사용 중인 아이디입니다.");
        this.errorCode = ErrorCode.DUPLICATE_LOGIN_ID;
    }
}