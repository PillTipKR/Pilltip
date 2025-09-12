package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class NotExistSupplementException extends IllegalStateException {
    private final ErrorCode errorCode;

    public NotExistSupplementException() {
        super(ErrorCode.NOT_EXIST_SUPPLEMENT.getMessage());
        this.errorCode = ErrorCode.NOT_EXIST_SUPPLEMENT;
    }
}