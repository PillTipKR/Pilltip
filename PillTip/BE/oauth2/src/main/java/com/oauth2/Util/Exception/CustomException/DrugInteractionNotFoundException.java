package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class DrugInteractionNotFoundException extends IllegalStateException {
    private final ErrorCode errorCode;

    public DrugInteractionNotFoundException() {
        super(ErrorCode.DRUG_INTERACTION_NOT_FOUND.getMessage());
        this.errorCode = ErrorCode.DRUG_INTERACTION_NOT_FOUND;
    }
}