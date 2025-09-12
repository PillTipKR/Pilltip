package com.oauth2.Util.Exception.CustomException;


import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class NotExistDosageLogException extends IllegalStateException {
    private final ErrorCode errorCode;

    public NotExistDosageLogException() {
        super(ErrorCode.NOT_EXIST_DOSAGELOG.getMessage());
        this.errorCode = ErrorCode.NOT_EXIST_DOSAGELOG;
    }

}
