package org.springframework.context;

import org.springframework.beans.BeanFactory;
import org.springframework.context.event.ApplicationEventPublisher;
import org.springframework.core.exception.BeansException;
import org.springframework.core.io.ResourceLoader;


/**
 * 应用上下文，Spring核心组件之一
 * 它继承并组合了BeanFactory、ResourceLoader、ApplicationEventPublisher等好用的组件，将复杂的应用上下文进行封装，让容器更好用
 */
public interface ApplicationContext extends BeanFactory, ResourceLoader, ApplicationEventPublisher {

    /**
     * 刷新容器
     */
    void refresh() throws BeansException;

    /**
     * 关闭应用上下文
     */
    void close();

    /**
     * 向虚拟机中注册一个钩子方法，在虚拟机关闭之前执行关闭容器等操作
     */
    void registerShutdownHook();
}
