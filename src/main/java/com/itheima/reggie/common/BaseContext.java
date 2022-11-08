package com.itheima.reggie.common;

/**
 * 保存和获取当前线程的用户Id
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 设置值
     * @param empId
     */
    public static void setThreadId(Long empId){
        threadLocal.set(empId);
    }

    /**
     * 获取值
     * @return
     */
    public static Long getThreadId(){
        return threadLocal.get();
    }
}
