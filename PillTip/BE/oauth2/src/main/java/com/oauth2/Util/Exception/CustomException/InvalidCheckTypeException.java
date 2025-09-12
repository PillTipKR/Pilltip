package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class InvalidCheckTypeException extends IllegalStateException {
    private final ErrorCode errorCode;

    public InvalidCheckTypeException() {
        super("유효하지 않은 체크 타입입니다.");
        this.errorCode = ErrorCode.INVALID_CHECK_TYPE;
    }
}