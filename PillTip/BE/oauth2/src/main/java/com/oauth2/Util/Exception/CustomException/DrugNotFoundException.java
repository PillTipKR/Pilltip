package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class DrugNotFoundException extends IllegalStateException {
    private final ErrorCode errorCode;

    public DrugNotFoundException() {
        super(ErrorCode.DRUG_NOT_FOUND.getMessage());
        this.errorCode = ErrorCode.DRUG_NOT_FOUND;
    }
}