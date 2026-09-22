package com.shop.common;

import lombok.Data;

/**
 * 统一接口返回结果
 *
 * @param <T> 数据类型
 */
@Data
public class Result<T> {

    /** 业务状态码：200 成功，400 参数/业务错误，401 未登录，403 无权限，500 系统异常 */
    private Integer code;

    /** 提示信息 */
    private String msg;

    /** 返回数据 */
    private T data;

    public static final int CODE_SUCCESS = 200;
    public static final int CODE_BAD_REQUEST = 400;
    public static final int CODE_UNAUTHORIZED = 401;
    public static final int CODE_FORBIDDEN = 403;
    public static final int CODE_ERROR = 500;

    public static <T> Result<T> success() {
        return build(CODE_SUCCESS, "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return build(CODE_SUCCESS, "操作成功", data);
    }

    public static <T> Result<T> success(String msg, T data) {
        return build(CODE_SUCCESS, msg, data);
    }

    public static <T> Result<T> error(String msg) {
        return build(CODE_BAD_REQUEST, msg, null);
    }

    public static <T> Result<T> error(int code, String msg) {
        return build(code, msg, null);
    }

    private static <T> Result<T> build(int code, String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }
}

