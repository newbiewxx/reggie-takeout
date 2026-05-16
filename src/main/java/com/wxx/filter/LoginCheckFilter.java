package com.wxx.filter;

import com.alibaba.fastjson.JSON;
import com.wxx.common.BaseContext;
import com.wxx.common.BaseContextPlus;
import com.wxx.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*") // 过滤器名及拦截路径
@Slf4j // Lombok：自动生成 log 日志对象
public class LoginCheckFilter implements Filter { // 登录校验过滤器

    // 路径匹配器，支持通配符匹配（如 /backend/**）
    public static final AntPathMatcher PATTERN_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest; // 强转为 HTTP 请求
        HttpServletResponse response = (HttpServletResponse) servletResponse; // 强转为 HTTP 响应

        String requestURI = request.getRequestURI(); // 获取当前请求路径
        log.info("拦截到请求：{}", requestURI);

        try {
            // 不需要处理的请求路径（登录、退出、静态资源等）
            String[] urls = new String[]{
                    "/employee/login",    // 登录接口
                    "/employee/logout",   // 退出接口
                    "/backend/**",        // 后台静态资源
                    "/front/**",          // 前端静态资源
            };

            // 判断当前请求是否匹配放行路径
            boolean isMatch = check(urls, requestURI);

            if (isMatch) {
                log.info("放行请求：{}", requestURI);
                filterChain.doFilter(request, response); // 直接放行
                return;
            }

            // 需要登录校验的请求：从 Session 获取登录用户 ID
            Long empId = BaseContext.getCurrentEmployeeId();

            if (empId != null) {
                log.info("用户已登录 - ID：{}", empId);
                // 将用户 ID 存入 ThreadLocal，供后续业务层使用（如 MetaObjectHandler 自动填充）
                BaseContextPlus.setCurrentId(empId);
                filterChain.doFilter(request, response); // 放行
                return;
            }

            // 未登录，返回错误信息
            log.info("用户未登录，请求路径：{}", requestURI);
            response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));

        } finally {
            // 请求结束后清理 ThreadLocal，防止内存泄漏
            BaseContextPlus.removeCurrentId();
        }
    }

    /**
     * 判断请求路径是否匹配放行列表
     * @param urls       放行路径模式
     * @param requestURI 当前请求路径
     * @return true=放行，false=需登录校验
     */
    public boolean check(String[] urls, String requestURI) {
        for (String url : urls) {
            boolean match = PATTERN_MATCHER.match(url, requestURI);
            if (match) {
                return true; // 匹配到放行路径
            }
        }
        return false; // 无匹配，需要登录
    }
}
