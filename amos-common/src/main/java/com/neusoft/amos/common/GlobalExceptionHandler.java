package com.neusoft.amos.common;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * 全局异常处理：将未捕获异常统一包装为 {@link ApiResponse}。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handle(Exception e) {
        return ApiResponse.fail(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegal(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(400, e.getMessage(), null));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatus(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode())
                .body(new ApiResponse<>(e.getStatusCode().value(), e.getReason(), null));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(DataIntegrityViolationException e) {
        String msg = (e.getMessage() != null && e.getMessage().contains("Unique"))
                ? "编码已存在，违反唯一约束"
                : "数据完整性约束冲突";
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse<>(409, msg, null));
    }
}
