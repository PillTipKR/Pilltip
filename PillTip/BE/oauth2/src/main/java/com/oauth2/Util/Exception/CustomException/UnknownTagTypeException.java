package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class UnknownTagTypeException extends IllegalStateException {
    private final ErrorCode errorCode;

    public UnknownTagTypeException() {
        super(ErrorCode.UNKNOWN_TAG_TYPE.getMessage());
        this.errorCode = ErrorCode.UNKNOWN_TAG_TYPE;
    }
}