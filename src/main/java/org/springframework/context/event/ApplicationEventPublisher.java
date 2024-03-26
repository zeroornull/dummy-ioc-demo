package org.springframework.context.event;


import org.springframework.context.event.event.ApplicationEvent;

/**
 * 事件发布者接口
 */
public interface ApplicationEventPublisher {

    /**
     * 发布事件
     */
    void publishEvent(ApplicationEvent event);
}
