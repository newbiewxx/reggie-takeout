package com.wxx.exception; // 公共工具包

import com.wxx.common.Constants;
import lombok.Getter;

/**
 * 自定义业务异常
 * 由全局异常处理器 GlobalExceptionHandler 统一捕获并封装为 R.error() 返回
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 错误码（复用 Constants.SUCCESS/FAIL，默认 FAIL=0） */
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = Constants.FAIL;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

}
