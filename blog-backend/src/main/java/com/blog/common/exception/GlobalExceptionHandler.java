package com.blog.common.exception;

import com.blog.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** 全局异常处理：业务错误一律 HTTP 200 + Result.code；静态资源缺失返回 404。 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<Void> biz(BizException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> valid(BindException e) {
        FieldError fe = e.getBindingResult().getFieldError();
        String msg = fe == null ? "参数错误" : fe.getField() + " " + fe.getDefaultMessage();
        return Result.error(400, msg);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public Result<Void> param(Exception e) {
        return Result.error(400, "参数错误: " + e.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> uploadSize(MaxUploadSizeExceededException e) {
        return Result.error(400, "文件大小超出限制（50MB）");
    }

    @ExceptionHandler(AuthenticationException.class)
    public Result<Void> auth(AuthenticationException e) {
        return Result.error(401, "未登录或登录已过期");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> denied(AccessDeniedException e) {
        return Result.error(403, "无权限");
    }

    /** 静态资源 404 保持 HTTP 404。 */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> notFound(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> other(Exception e) {
        log.error("服务器内部错误", e);
        return Result.error(500, "服务器内部错误");
    }
}
