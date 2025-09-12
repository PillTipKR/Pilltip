package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class AlreadyTakenException extends IllegalStateException {
    private final ErrorCode errorCode;

    public AlreadyTakenException() {
        super(ErrorCode.ALREADY_TAKEN.getMessage());
        this.errorCode = ErrorCode.ALREADY_TAKEN;
    }
}