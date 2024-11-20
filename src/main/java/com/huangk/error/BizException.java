package com.huangk.error;

public class BizException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    protected Integer errorCode;

    protected String errorMsg;

    public BizException() {
        super();
    }

    public BizException(IError errorInfoInterface) {
        super(errorInfoInterface.getMsg());
        this.errorCode = errorInfoInterface.getCode();
        this.errorMsg = errorInfoInterface.getMsg();
    }

    public BizException(IError errorInfoInterface, Throwable cause) {
        super(errorInfoInterface.getMsg(), cause);
        this.errorCode = errorInfoInterface.getCode();
        this.errorMsg = errorInfoInterface.getMsg();
    }

    public BizException(String errorMsg) {
        super(errorMsg);
        this.errorMsg = errorMsg;
    }

    public BizException(Integer errorCode, String errorMsg) {
        super(errorMsg);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public BizException(Integer errorCode, String errorMsg, Throwable cause) {
        super(errorMsg, cause);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }


    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
