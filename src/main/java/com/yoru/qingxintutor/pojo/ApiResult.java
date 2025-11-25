package com.yoru.qingxintutor.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiResult<T> {
    private Integer code;
    private String message;
    private T data;

    private ApiResult(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(1, "Success", data);
    }

    public static <T> ApiResult<T> success() {
        return new ApiResult<>(1, "Success", null);
    }

    public static <T> ApiResult<T> error() {
        return new ApiResult<>(0, "Fail", null);
    }

    public static <T> ApiResult<T> error(String message) {
        return new ApiResult<>(0, message, null);
    }
}
