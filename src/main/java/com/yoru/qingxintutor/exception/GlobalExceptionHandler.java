package com.yoru.qingxintutor.exception;

import com.yoru.qingxintutor.pojo.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.FileNotFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResult<Void> handleIllegalArgument(IllegalArgumentException e) {
        return ApiResult.error(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult<Void> handleValidationErrors(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse("Invalid input");
        return ApiResult.error(errorMessage);
    }
    
    @ExceptionHandler(BusinessException.class)
    public ApiResult<?> handleBusiness(BusinessException e) {
        return ApiResult.error(e.getMessage());
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ApiResult<Void> handleFileNotFoundErrors(FileNotFoundException e) {
        return ApiResult.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResult<Void> handleGeneric(Exception e) {
        log.error("Unexpected error: {}", e.getMessage());
        return ApiResult.error("An unexpected error occurred! Please contact to admin.");
    }
}