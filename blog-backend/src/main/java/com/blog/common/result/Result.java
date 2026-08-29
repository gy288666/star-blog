package com.blog.common.result;

import lombok.Data;

/**
 * 统一返回体，code 语义见 docs/api-contract.md：0 成功，400 参数/业务错误，
 * 401 未认证，403 无权限，500 服务器错误。HTTP 状态一律 200。
 */
@Data
public class Result<T> {

    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> ok() {
        return build(0, "ok", null);
    }

    public static <T> Result<T> ok(T data) {
        return build(0, "ok", data);
    }

    public static <T> Result<T> error(int code, String msg) {
        return build(code, msg, null);
    }

    public static <T> Result<T> build(int code, String msg, T data) {
        Result<T> r = new Result<>();
        r.code = code;
        r.msg = msg;
        r.data = data;
        return r;
    }
}
