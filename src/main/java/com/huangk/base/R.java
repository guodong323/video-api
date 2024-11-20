package com.huangk.base;

import com.huangk.error.ErrorEnum;
import com.huangk.error.IError;
import lombok.Data;

/**
 * @author zhanghao
 */
@Data
public class R<T> {
    /**
     * 响应代码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 响应结果
     */
    private T data;

    public R() {
    }

    public R(IError errorInfo) {
        this.code = errorInfo.getCode();
        this.msg = errorInfo.getMsg();
    }

    /**
     * 成功
     *
     * @return
     */
    public static R<Void> ok() {
        R<Void> rb = new R<>();
        rb.setCode(ErrorEnum.SUCCESS.getCode());
        rb.setMsg(ErrorEnum.SUCCESS.getMsg());
        return rb;
    }

    /**
     * 成功
     *
     * @param data
     * @return
     */
    public static <T> R<T> ok(T data) {
        R<T> rb = new R<>();
        rb.setCode(ErrorEnum.SUCCESS.getCode());
        rb.setMsg(ErrorEnum.SUCCESS.getMsg());
        rb.setData(data);
        return rb;
    }

    /**
     * 失败
     */
    public static R<Void> fail(IError errorInfo) {
        R<Void> rb = new R<>();
        rb.setCode(errorInfo.getCode());
        rb.setMsg(errorInfo.getMsg());
        return rb;
    }

    /**
     * 失败
     */
    public static R<Void> fail(Integer code, String message) {
        R<Void> rb = new R<>();
        rb.setCode(code);
        rb.setMsg(message);
        return rb;
    }

    /**
     * 失败
     */
    public static <T> R<T> fail(String message) {
        R<T> rb = new R<>();
        rb.setCode(-1);
        rb.setMsg(message);
        return rb;
    }

}
