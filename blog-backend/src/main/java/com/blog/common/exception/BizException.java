package com.blog.common.exception;

import lombok.Getter;

/** 业务异常，code 直接进 Result.code。 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String message) {
        this(400, message);
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }
}
