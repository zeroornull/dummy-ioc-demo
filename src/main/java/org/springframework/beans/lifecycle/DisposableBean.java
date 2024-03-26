package org.springframework.beans.lifecycle;

/**
 * Bean生命周期钩子：Bean销毁前调用
 * 如果实现该接口，则在Bean销毁前会执行destroy方法（容器关闭或启动异常等）
 */
public interface DisposableBean {

    void destroy() throws Exception;
}
