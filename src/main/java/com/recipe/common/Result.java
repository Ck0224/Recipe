package com.recipe.common;

import lombok.Data;

/**
 * 全局统一返回结果
 */
@Data
public class Result<T> {
    // 响应码：200成功，500失败，401未登录，403无权限
    private Integer code;
    // 响应消息
    private String message;
    // 响应数据
    private T data;

    // 成功响应（带数据）
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    // 成功响应（无数据）
    public static <T> Result<T> success() {
        return success(null);
    }

    // 失败响应
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        result.setData(null);
        return result;
    }

    // 未登录/Token过期
    public static <T> Result<T> unAuthorized() {
        Result<T> result = new Result<>();
        result.setCode(401);
        result.setMessage("未登录或Token已过期");
        result.setData(null);
        return result;
    }

    // 无权限
    public static <T> Result<T> forbidden() {
        Result<T> result = new Result<>();
        result.setCode(403);
        result.setMessage("无操作权限");
        result.setData(null);
        return result;
    }
}
