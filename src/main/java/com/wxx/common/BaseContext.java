package com.wxx.common; // 公共工具包

import org.springframework.web.context.request.RequestContextHolder; // Spring 请求上下文持有器
import org.springframework.web.context.request.ServletRequestAttributes; // Spring Servlet 请求属性

import javax.servlet.http.HttpServletRequest; // HTTP 请求对象
import javax.servlet.http.HttpSession; // HTTP 会话对象

/**
 * 当前用户上下文工具类
 * 通过 RequestContextHolder 获取当前线程绑定的请求，避免在 Controller 方法参数中直接注入 HttpServletRequest
 */
public class BaseContext {

    // 员工 ID 在 Session 中存储的 key
    private static final String EMPLOYEE_SESSION_KEY = "employee";

    /**
     * 获取当前请求的 HttpServletRequest
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attributes.getRequest();
    }

    /**
     * 获取当前请求的 Session
     */
    public static HttpSession getSession() {
        return getRequest().getSession();
    }

    /**
     * 设置当前登录员工 ID 到 Session
     * @param id 员工 ID
     */
    public static void setCurrentEmployeeId(Long id) {
        getSession().setAttribute(EMPLOYEE_SESSION_KEY, id);
    }

    /**
     * 获取当前登录员工 ID
     * @return 员工 ID，未登录返回 null
     */
    public static Long getCurrentEmployeeId() {
        return (Long) getSession().getAttribute(EMPLOYEE_SESSION_KEY);
    }

    /**
     * 清除当前登录员工信息（退出登录）
     */
    public static void removeCurrentEmployeeId() {
        getSession().removeAttribute(EMPLOYEE_SESSION_KEY);
    }
}
