package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class TermsAgreementFailedException extends IllegalStateException {
    private final ErrorCode errorCode;

    public TermsAgreementFailedException() {
        super(ErrorCode.TERMS_AGREEMENT_FAILED.getMessage());
        this.errorCode = ErrorCode.TERMS_AGREEMENT_FAILED;
    }
}