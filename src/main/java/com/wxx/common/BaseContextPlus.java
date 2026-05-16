package com.wxx.common; // 公共工具包

/**
 * 基于 ThreadLocal 的当前用户 ID 持有器
 * 与 BaseContext（基于 RequestContextHolder + Session）不同，
 * 此工具类直接将用户 ID 存储在当前线程中，不依赖 HttpSession，
 * 适用于不需要跨请求持久化 Session 的场景
 */
public class BaseContextPlus {

    // ThreadLocal：每个线程独享一份副本，线程安全
    private static final ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 设置当前用户 ID 到当前线程
     * @param id 用户 ID
     */
    public static void setCurrentId(Long id) {
        threadLocal.set(id); // 将 ID 存入当前线程的 ThreadLocal
    }

    /**
     * 从当前线程获取用户 ID
     * @return 用户 ID，未设置时返回 null
     */
    public static Long getCurrentId() {
        return threadLocal.get(); // 从当前线程的 ThreadLocal 取出 ID
    }

    /**
     * 清除当前线程的用户 ID（防止内存泄漏）
     * 建议在请求结束时（如 Filter 的 finally 块中）调用
     */
    public static void removeCurrentId() {
        threadLocal.remove(); // 清理 ThreadLocal，避免内存泄漏
    }
}