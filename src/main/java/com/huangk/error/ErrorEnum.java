package com.huangk.error;

public enum ErrorEnum implements IError {
    // 数据操作错误定义
    SUCCESS(200, "SUCCESS"),
    BODY_NOT_MATCH(400, "The requested data format does not match!"),
    SIGNATURE_NOT_MATCH(401, "The requested digital signature does not match!"),
    NOT_FOUND(404, "Not found!"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error!"),
    SERVER_BUSY(503, "The server is busy!");

    private final Integer code;

    private final String msg;

    ErrorEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }
}
