package com.oauth2.Util.Exception.CustomException;


import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class MissingFCMTokenException extends IllegalStateException {
    private final ErrorCode errorCode;

    public MissingFCMTokenException() {
        super(ErrorCode.MISSING_FCMTOKEN.getMessage());
        this.errorCode = ErrorCode.MISSING_FCMTOKEN;
    }

}
