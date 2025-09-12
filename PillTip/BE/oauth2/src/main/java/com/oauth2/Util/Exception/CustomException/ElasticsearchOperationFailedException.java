package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class ElasticsearchOperationFailedException extends IllegalStateException {
    private final ErrorCode errorCode;

    public ElasticsearchOperationFailedException() {
        super(ErrorCode.ELASTICSEARCH_OPERATION_FAILED.getMessage());
        this.errorCode = ErrorCode.ELASTICSEARCH_OPERATION_FAILED;
    }
}