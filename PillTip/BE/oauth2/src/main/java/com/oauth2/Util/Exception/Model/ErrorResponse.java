package com.oauth2.Util.Exception.Model;


import jakarta.validation.ConstraintViolation; // Bean Validation 제약 조건 위반 정보를 다룸
import lombok.AccessLevel;
import lombok.Getter; // Lombok: getter 자동 생성
import lombok.NoArgsConstructor; // Lombok: 기본 생성자 자동 생성
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult; // Spring Validation 결과를 담는 객체
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException; // 잘못된 타입 매핑 예외

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자를 protected로 생성
public class ErrorResponse {

    private HttpStatus status;      // HTTP 상태 코드 (예: 400, 404)
    private String code;     // 에러 코드 문자열 (예: "INVALID_INPUT")
    private String message;  // 에러 메시지
    private List<FieldError> errors; // 필드 오류 리스트

    // 에러 코드 + 필드 에러 목록을 담은 생성자
    private ErrorResponse(final ErrorCode code, final List<FieldError> errors) {
        this.status = code.getStatus();
        this.code = code.getCode();
        this.message = code.getMessage();
        this.errors = errors;
    }

    // 에러 코드만 담는 생성자 (필드 에러가 없는 경우)
    private ErrorResponse(final ErrorCode code) {
        this.status = code.getStatus();
        this.code = code.getCode();
        this.message = code.getMessage();
        this.errors = new ArrayList<>(); // 빈 리스트로 초기화
    }

    // BindingResult 기반 오류 생성 (form validation 실패 등)
    public static ErrorResponse of(final ErrorCode code, final BindingResult bindingResult) {
        return new ErrorResponse(code, FieldError.of(bindingResult));
    }

    // ConstraintViolation 기반 오류 생성 (DTO에 @Valid 사용 시 발생)
    public static ErrorResponse of(final ErrorCode code, final Set<ConstraintViolation<?>> constraintViolations) {
        return new ErrorResponse(code, FieldError.of(constraintViolations));
    }

    // 누락된 파라미터 처리용 오류 응답 생성
    public static ErrorResponse of(final ErrorCode code, final String missingParameterName) {
        return new ErrorResponse(code, FieldError.of(missingParameterName, "", "parameter must required"));
    }

    // 단순 에러 코드만 있을 때 사용
    public static ErrorResponse of(final ErrorCode code) {
        return new ErrorResponse(code);
    }

    // 외부에서 직접 FieldError 리스트를 주입할 때 사용
    public static ErrorResponse of(final ErrorCode code, final List<FieldError> errors) {
        return new ErrorResponse(code, errors);
    }

    // 파라미터 타입 불일치 예외 처리용 응답 생성
    public static ErrorResponse of(MethodArgumentTypeMismatchException e) {
        final String value = e.getValue() == null ? "" : e.getValue().toString();
        final List<FieldError> errors = FieldError.of(e.getName(), value, e.getErrorCode());
        return new ErrorResponse(ErrorCode.INVALID_TYPE_VALUE, errors);
    }

    // 내부 클래스: 필드 단위 에러 정보 표현
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class FieldError {
        private String field;   // 오류가 발생한 필드명
        private String value;   // 잘못된 값
        private String reason;  // 오류 사유

        public FieldError(String field, String value, String reason) {
            this.field = field;
            this.value = value;
            this.reason = reason;
        }

        // 단일 필드 오류로 리스트 생성
        public static List<FieldError> of(final String field, final String value, final String reason) {
            List<FieldError> fieldErrors = new ArrayList<>();
            fieldErrors.add(new FieldError(field, value, reason));
            return fieldErrors;
        }

        // BindingResult → FieldError 변환
        public static List<FieldError> of(final BindingResult bindingResult) {
            final List<org.springframework.validation.FieldError> fieldErrors = bindingResult.getFieldErrors();
            return fieldErrors.stream()
                    .map(error -> new FieldError(
                            error.getField(),
                            error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                            error.getDefaultMessage()
                    ))
                    .collect(Collectors.toList());
        }

        // ConstraintViolation → FieldError 변환
        public static List<FieldError> of(final Set<ConstraintViolation<?>> constraintViolations) {
            List<ConstraintViolation<?>> lists = new ArrayList<>(constraintViolations);
            return lists.stream()
                    .map(error -> new FieldError(
                            error.getPropertyPath().toString(), // 필드 경로
                            "",
                            error.getMessageTemplate() // 메시지 템플릿 사용
                    ))
                    .collect(Collectors.toList());
        }
    }
}