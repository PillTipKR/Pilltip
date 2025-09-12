package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class NotExistIngredientException extends IllegalStateException {
    private final ErrorCode errorCode;

    public NotExistIngredientException() {
        super(ErrorCode.NOT_EXIST_INGREDIENT.getMessage());
        this.errorCode = ErrorCode.NOT_EXIST_INGREDIENT;
    }
}