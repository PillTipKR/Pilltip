package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class UnsupportedDrugTypeException extends IllegalStateException {
    private final ErrorCode errorCode;

    public UnsupportedDrugTypeException() {
        super(ErrorCode.UNSUPPORTED_DRUG_TYPE.getMessage());
        this.errorCode = ErrorCode.UNSUPPORTED_DRUG_TYPE;
    }
}