package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class DosageTimeNotPassedException extends IllegalStateException {
    private final ErrorCode errorCode;

    public DosageTimeNotPassedException() {
        super(ErrorCode.DOSAGE_TIME_NOT_PASSED.getMessage());
        this.errorCode = ErrorCode.DOSAGE_TIME_NOT_PASSED;
    }
}