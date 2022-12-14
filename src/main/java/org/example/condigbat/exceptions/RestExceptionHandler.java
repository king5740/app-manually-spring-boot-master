package org.example.condigbat.exceptions;

import org.apache.catalina.connector.Response;
import org.example.condigbat.payload.ApiResult;
import org.example.condigbat.payload.ErrorData;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(value = RestException.class)
    public ResponseEntity<ApiResult<List<ErrorData>>> exceptionHandle(RestException ex) {
        ApiResult<List<ErrorData>> result =
                ApiResult.failResponse(ex.getMessage(),
                        ex.getStatus().value());
        return new ResponseEntity<>(result, ex.getStatus());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<List<ErrorData>>> exceptionHandle(
            MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

        List<ErrorData> errorDataList = new ArrayList<>();

        for (FieldError fieldError : fieldErrors)
            errorDataList.add(
                    new ErrorData(fieldError.getDefaultMessage(),
                            HttpStatus.BAD_REQUEST.value(),
                            fieldError.getField()));

        ApiResult<List<ErrorData>> apiResult = ApiResult.failResponse(errorDataList);
        return new ResponseEntity<>(apiResult, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = EmptyResultDataAccessException.class)
    public ResponseEntity<ApiResult<List<ErrorData>>> exceptionHandle(EmptyResultDataAccessException ex) {
        ApiResult<List<ErrorData>> result =
                ApiResult.failResponse(ex.getMessage(),
                        HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<ApiResult<List<ErrorData>>> exceptionHandle(AccessDeniedException ex) {
        ApiResult<List<ErrorData>> apiResult = ApiResult.failResponse(
                "Huquqingiz yo'q okasi",
                HttpStatus.FORBIDDEN.value());
        return new ResponseEntity<>(apiResult, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = InsufficientAuthenticationException.class)
    public ResponseEntity<ApiResult<List<ErrorData>>> exceptionHandle(InsufficientAuthenticationException ex) {
        ApiResult<List<ErrorData>> apiResult = ApiResult.failResponse(
                "Full authentication is required to access this resource",
                HttpStatus.UNAUTHORIZED.value());
        return new ResponseEntity<>(apiResult, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = Throwable.class)
    public ResponseEntity<ApiResult<List<ErrorData>>> exceptionHandle(Throwable ex) {
        ApiResult<List<ErrorData>> apiResult = ApiResult.failResponse(
                "Full authentication is required to access this resource",
                HttpStatus.UNAUTHORIZED.value());
        return new ResponseEntity<>(apiResult, HttpStatus.UNAUTHORIZED);
    }


}
