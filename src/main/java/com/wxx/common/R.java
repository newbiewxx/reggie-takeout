package com.wxx.common;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

@Data
public class R<T> {

    private Integer code; //编码：1成功，0和其它数字为失败

    private String msg; //错误信息

    private T data; //数据

    private Map<Object, Object> map = new HashMap<>(); //动态数据 附加数据

    public static <T> R<T> success(T data) {
        R<T> r = new R<T>();
        r.data = data;
        r.code = Constants.SUCCESS;
        return r;
    }

    public static <T> R<T> error(String msg) {
        R<T> r = new R<>();
        r.msg = msg;
        r.code = Constants.FAIL;
        return r;
    }

    public R<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

}
