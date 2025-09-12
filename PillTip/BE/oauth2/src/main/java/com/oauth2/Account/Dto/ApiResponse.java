// author : mireutale
// description : API 응답 정보, DTO(Data Transfer Object) 사용

package com.oauth2.Account.Dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ApiResponse<T> {
    // 응답 결과
    private String status; // "success" 또는 "error"
    private String message; // 메시지
    private T data; // 데이터

    // 성공 응답, 기본 메세지 "Success"
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .status("success")
                .data(data)
                .build();
    }

    // 성공 응답, 커스텀 메세지 포함
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .status("success")
                .message(message)
                .data(data)
                .build();
    }

    // 실패 응답, 커스템 메세지 포함
    public static <T> ApiResponse<T> error(String message, T data) {
        return ApiResponse.<T>builder()
                .status("error")
                .message(message)
                .data(data)
                .build();
    }
}
