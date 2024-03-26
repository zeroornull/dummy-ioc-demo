package org.springframework.context.event.multicaster;


import org.springframework.context.event.event.ApplicationEvent;
import org.springframework.context.event.listener.ApplicationListener;

/**
 * 事件发布器
 */
public interface ApplicationEventMulticaster {

    void addApplicationListener(ApplicationListener<?> listener);

    // createBean失败时移除监听器（如果当前bean是一个listener的话）
    void removeApplicationListener(ApplicationListener<?> listener);

    void multicastEvent(ApplicationEvent event);

}
