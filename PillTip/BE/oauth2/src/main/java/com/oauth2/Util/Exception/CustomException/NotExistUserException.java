package com.oauth2.Util.Exception.CustomException;


import com.oauth2.Util.Exception.Model.ErrorCode;
import jakarta.persistence.EntityNotFoundException;
import lombok.Getter;

@Getter
public class NotExistUserException extends EntityNotFoundException {
    private final ErrorCode errorCode;

    public NotExistUserException() {
        super(ErrorCode.NOT_EXIST_USER.getMessage());
        this.errorCode = ErrorCode.NOT_EXIST_USER;
    }

}
