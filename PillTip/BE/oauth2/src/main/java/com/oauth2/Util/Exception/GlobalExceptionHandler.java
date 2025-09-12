package com.oauth2.Util.Exception;

import com.oauth2.Util.Exception.CustomException.*;
import com.oauth2.Util.Exception.Model.ErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.apache.catalina.connector.ClientAbortException;
import java.io.IOException;

import static com.oauth2.Util.Exception.Model.ErrorCode.*;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleBadCredentialException(BadCredentialsException e) {
        final ErrorResponse response = ErrorResponse.of(BAD_CREDENTIALS);
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException e) {
        final ErrorResponse response = ErrorResponse.of(INVALID_INPUT_VALUE, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        final ErrorResponse response = ErrorResponse.of(e);
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        final ErrorResponse response = ErrorResponse.of(METHOD_NOT_ALLOWED);
        return new ResponseEntity<>(response, response.getStatus());
    }

    // @Valid, @Validated 에서 binding error 발생 시 (@RequestBody)
    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleBindException(BindException e) {
        final ErrorResponse response = ErrorResponse.of(INVALID_INPUT_VALUE, e.getBindingResult());
        return new ResponseEntity<>(response, response.getStatus());
    }


    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleMissingFCMException(MissingFCMTokenException e) {
        final ErrorResponse response = ErrorResponse.of(MISSING_FCMTOKEN, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleNotExistDosagelogException(NotExistDosageLogException e) {
        final ErrorResponse response = ErrorResponse.of(NOT_EXIST_DOSAGELOG, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleNotFriendException(NotFriendException e) {
        final ErrorResponse response = ErrorResponse.of(NOT_FRIEND, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleNotExistUserException(NotExistUserException e) {
        final ErrorResponse response = ErrorResponse.of(NOT_EXIST_USER, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleUserNotAuthenticatedException(UserNotAuthenticatedException e) {
        final ErrorResponse response = ErrorResponse.of(USER_NOT_AUTHENTICATED, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleTokenNotProvidedException(TokenNotProvidedException e) {
        final ErrorResponse response = ErrorResponse.of(TOKEN_NOT_PROVIDED, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleInvalidTokenException(InvalidTokenException e) {
        final ErrorResponse response = ErrorResponse.of(INVALID_TOKEN, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleTokenExpiredException(TokenExpiredException e) {
        final ErrorResponse response = ErrorResponse.of(TOKEN_EXPIRED, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleInvalidRefreshTokenException(InvalidRefreshTokenException e) {
        final ErrorResponse response = ErrorResponse.of(INVALID_REFRESH_TOKEN, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleAuthenticationFailedException(AuthenticationFailedException e) {
        final ErrorResponse response = ErrorResponse.of(AUTHENTICATION_FAILED, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleDosageTimeNotPassedException(DosageTimeNotPassedException e) {
        final ErrorResponse response = ErrorResponse.of(DOSAGE_TIME_NOT_PASSED, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleAlreadyTakenException(AlreadyTakenException e) {
        final ErrorResponse response = ErrorResponse.of(ALREADY_TAKEN, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleOperationFailedException(OperationFailedException e) {
        final ErrorResponse response = ErrorResponse.of(OPERATION_FAILED, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleInvalidRequestException(InvalidRequestException e) {
        final ErrorResponse response = ErrorResponse.of(INVALID_REQUEST, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleSerializationFailedException(SerializationFailedException e) {
        final ErrorResponse response = ErrorResponse.of(SERIALIZATION_FAILED, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleDeserializationFailedException(DeserializationFailedException e) {
        final ErrorResponse response = ErrorResponse.of(DESERIALIZATION_FAILED, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleFileUploadFailedException(FileUploadFailedException e) {
        final ErrorResponse response = ErrorResponse.of(FILE_UPLOAD_FAILED, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleFileSaveFailedException(FileSaveFailedException e) {
        final ErrorResponse response = ErrorResponse.of(FILE_SAVE_FAILED, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleDirectoryCreateFailedException(DirectoryCreateFailedException e) {
        final ErrorResponse response = ErrorResponse.of(DIRECTORY_CREATE_FAILED, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleLoginFailedException(LoginFailedException e) {
        final ErrorResponse response = ErrorResponse.of(LOGIN_FAILED, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleSignupFailedException(SignupFailedException e) {
        final ErrorResponse response = ErrorResponse.of(SIGNUP_FAILED, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleLogoutFailedException(LogoutFailedException e) {
        final ErrorResponse response = ErrorResponse.of(LOGOUT_FAILED, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleTokenRefreshFailedException(TokenRefreshFailedException e) {
        final ErrorResponse response = ErrorResponse.of(TOKEN_REFRESH_FAILED, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleLoginTypeRequiredException(LoginTypeRequiredException e) {
        final ErrorResponse response = ErrorResponse.of(LOGIN_TYPE_REQUIRED, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleSocialAccountAlreadyExistsException(SocialAccountAlreadyExistsException e) {
        final ErrorResponse response = ErrorResponse.of(SOCIAL_ACCOUNT_ALREADY_EXISTS, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleTermsAgreementFailedException(TermsAgreementFailedException e) {
        final ErrorResponse response = ErrorResponse.of(TERMS_AGREEMENT_FAILED, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleSearchFailedException(SearchFailedException e) {
        final ErrorResponse response = ErrorResponse.of(SEARCH_FAILED, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleUnsupportedOauth2ProviderException(UnsupportedOauth2ProviderException e) {
        final ErrorResponse response = ErrorResponse.of(UNSUPPORTED_OAUTH2_PROVIDER, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleOauth2UserIdRequiredException(Oauth2UserIdRequiredException e) {
        final ErrorResponse response = ErrorResponse.of(OAUTH2_USER_ID_REQUIRED, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleOauth2UserInfoParseFailedException(Oauth2UserInfoParseFailedException e) {
        final ErrorResponse response = ErrorResponse.of(OAUTH2_USER_INFO_PARSE_FAILED, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleSocialAccountAlreadyExistsDetailException(SocialAccountAlreadyExistsDetailException e) {
        final ErrorResponse response = ErrorResponse.of(SOCIAL_ACCOUNT_ALREADY_EXISTS_DETAIL, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleInvalidPhoneNumberFormatException(InvalidPhoneNumberFormatException e) {
        final ErrorResponse response = ErrorResponse.of(INVALID_PHONE_NUMBER_FORMAT, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleDrugNotFoundException(DrugNotFoundException e) {
        final ErrorResponse response = ErrorResponse.of(DRUG_NOT_FOUND, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleDrugInteractionNotFoundException(DrugInteractionNotFoundException e) {
        final ErrorResponse response = ErrorResponse.of(DRUG_INTERACTION_NOT_FOUND, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleUnsupportedDrugTypeException(UnsupportedDrugTypeException e) {
        final ErrorResponse response = ErrorResponse.of(UNSUPPORTED_DRUG_TYPE, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleReviewDeletePermissionDeniedException(ReviewDeletePermissionDeniedException e) {
        final ErrorResponse response = ErrorResponse.of(REVIEW_DELETE_PERMISSION_DENIED, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleUnknownTagTypeException(UnknownTagTypeException e) {
        final ErrorResponse response = ErrorResponse.of(UNKNOWN_TAG_TYPE, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleElasticsearchOperationFailedException(ElasticsearchOperationFailedException e) {
        final ErrorResponse response = ErrorResponse.of(ELASTICSEARCH_OPERATION_FAILED, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleJsonConversionFailedException(JsonConversionFailedException e) {
        final ErrorResponse response = ErrorResponse.of(JSON_CONVERSION_FAILED, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleJsonParseFailedException(JsonParseFailedException e) {
        final ErrorResponse response = ErrorResponse.of(JSON_PARSE_FAILED, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleUnexpectedErrorException(UnexpectedErrorException e) {
        final ErrorResponse response = ErrorResponse.of(UNEXPECTED_ERROR, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleValidationFailedException(ValidationFailedException e) {
        final ErrorResponse response = ErrorResponse.of(VALIDATION_FAILED, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleGoogleUserIdRequiredException(GoogleUserIdRequiredException e) {
        final ErrorResponse response = ErrorResponse.of(GOOGLE_USER_ID_REQUIRED, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleKakaoUserIdRequiredException(KakaoUserIdRequiredException e) {
        final ErrorResponse response = ErrorResponse.of(KAKAO_USER_ID_REQUIRED, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleGoogleUserInfoParseFailedException(GoogleUserInfoParseFailedException e) {
        final ErrorResponse response = ErrorResponse.of(GOOGLE_USER_INFO_PARSE_FAILED, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleKakaoUserInfoParseFailedException(KakaoUserInfoParseFailedException e) {
        final ErrorResponse response = ErrorResponse.of(KAKAO_USER_INFO_PARSE_FAILED, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleInvalidRefreshTokenDetailException(InvalidRefreshTokenDetailException e) {
        final ErrorResponse response = ErrorResponse.of(INVALID_REFRESH_TOKEN_DETAIL, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleTokenNotFoundException(TokenNotFoundException e) {
        final ErrorResponse response = ErrorResponse.of(TOKEN_NOT_FOUND, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleTokenExpiredDetailException(TokenExpiredDetailException e) {
        final ErrorResponse response = ErrorResponse.of(TOKEN_EXPIRED_DETAIL, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleTokenInvalidRetryLoginException(TokenInvalidRetryLoginException e) {
        final ErrorResponse response = ErrorResponse.of(TOKEN_INVALID_RETRY_LOGIN, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleTokenExpiredRetryLoginException(TokenExpiredRetryLoginException e) {
        final ErrorResponse response = ErrorResponse.of(TOKEN_EXPIRED_RETRY_LOGIN, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleUserInfoNotFoundRetryLoginException(UserInfoNotFoundRetryLoginException e) {
        final ErrorResponse response = ErrorResponse.of(USER_INFO_NOT_FOUND_RETRY_LOGIN, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleLoginTypeRequiredDetailException(LoginTypeRequiredDetailException e) {
        final ErrorResponse response = ErrorResponse.of(LOGIN_TYPE_REQUIRED_DETAIL, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleUserIdPasswordRequiredException(UserIdPasswordRequiredException e) {
        final ErrorResponse response = ErrorResponse.of(USER_ID_PASSWORD_REQUIRED, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleTokenRequiredForSocialException(TokenRequiredForSocialException e) {
        final ErrorResponse response = ErrorResponse.of(TOKEN_REQUIRED_FOR_SOCIAL, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleErrorKeywordLoginTypeException(ErrorKeywordLoginTypeException e) {
        final ErrorResponse response = ErrorResponse.of(ERROR_KEYWORD_LOGIN_TYPE, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleErrorKeywordPhoneNumberException(ErrorKeywordPhoneNumberException e) {
        final ErrorResponse response = ErrorResponse.of(ERROR_KEYWORD_PHONE_NUMBER, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleErrorKeywordNicknameException(ErrorKeywordNicknameException e) {
        final ErrorResponse response = ErrorResponse.of(ERROR_KEYWORD_NICKNAME, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleErrorKeywordUserIdException(ErrorKeywordUserIdException e) {
        final ErrorResponse response = ErrorResponse.of(ERROR_KEYWORD_USER_ID, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }


    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleProfileIsMainException(ProfileIsMainException e) {
        final ErrorResponse response = ErrorResponse.of(PROFILE_IS_MAIN, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }
    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleProfileIsMainException(InvalidProfileIdException e) {
        final ErrorResponse response = ErrorResponse.of(INVALID_PROFILE_ID, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleNotExistIngredientException(NotExistIngredientException e) {
        final ErrorResponse response = ErrorResponse.of(NOT_EXIST_INGREDIENT, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleNotExistSupplementException(NotExistSupplementException e) {
        final ErrorResponse response = ErrorResponse.of(NOT_EXIST_SUPPLEMENT, e.getMessage());
        return new ResponseEntity<>(response, response.getStatus());
    }

    // 클라이언트 연결 중단 관련 예외 처리
    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleAsyncRequestNotUsableException(AsyncRequestNotUsableException e) {
        log.warn("Client connection was aborted: {}", e.getMessage());
        // 클라이언트가 연결을 끊었으므로 응답을 보낼 필요가 없음
        return null;
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleClientAbortException(ClientAbortException e) {
        log.warn("Client connection was aborted: {}", e.getMessage());
        // 클라이언트가 연결을 끊었으므로 응답을 보낼 필요가 없음
        return null;
    }

    // IOException (Broken pipe 등) 처리
    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleIOException(IOException e) {
        if (e.getMessage() != null && e.getMessage().contains("Broken pipe")) {
            log.warn("Client connection was aborted (Broken pipe): {}", e.getMessage());
            return null;
        }
        log.error("IO Exception occurred: ", e);
        final ErrorResponse response = ErrorResponse.of(INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(response, response.getStatus());
    }

    // 그 밖에 발생하는 모든 예외처리가 이곳으로 모인다.
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        // 클라이언트 연결 중단 관련 예외는 로그만 남기고 응답하지 않음
        if (e instanceof AsyncRequestNotUsableException || 
            e instanceof ClientAbortException ||
            e.getCause() instanceof ClientAbortException ||
            (e instanceof IOException && e.getMessage() != null && e.getMessage().contains("Broken pipe"))) {
            log.warn("Client connection was aborted during exception handling: {}", e.getMessage());
            return null;
        }
        
        log.error("CustomException: ", e);
        final ErrorResponse response = ErrorResponse.of(INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(response, response.getStatus());
    }

}