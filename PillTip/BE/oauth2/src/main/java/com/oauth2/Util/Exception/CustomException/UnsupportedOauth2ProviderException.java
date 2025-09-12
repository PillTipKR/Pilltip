package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class UnsupportedOauth2ProviderException extends IllegalStateException {
    private final ErrorCode errorCode;

    public UnsupportedOauth2ProviderException() {
        super(ErrorCode.UNSUPPORTED_OAUTH2_PROVIDER.getMessage());
        this.errorCode = ErrorCode.UNSUPPORTED_OAUTH2_PROVIDER;
    }
}