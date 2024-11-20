package com.huangk.config;

import com.huangk.base.R;
import com.huangk.error.BizException;
import com.huangk.error.ErrorEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zhanghao
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义的业务异常
     *
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value = BizException.class)
    @ResponseBody
    public R<Void> bizExceptionHandler(HttpServletRequest req, BizException e) {
        log.error("发生业务异常！原因是：{}", e.getErrorMsg());
        return R.fail(e.getErrorCode(), e.getErrorMsg());
    }

    /**
     * 处理空指针的异常
     *
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value = NullPointerException.class)
    @ResponseBody
    public R<Void> exceptionHandler(HttpServletRequest req, NullPointerException e) {
        log.error("发生空指针异常！原因是:", e);
        return R.fail(ErrorEnum.BODY_NOT_MATCH);
    }

    /**
     * 处理其他异常
     *
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public R<Void> exceptionHandler(HttpServletRequest req, Exception e) {
        log.error("未知异常！原因是:", e);
        return R.fail(ErrorEnum.INTERNAL_SERVER_ERROR);
    }
}
