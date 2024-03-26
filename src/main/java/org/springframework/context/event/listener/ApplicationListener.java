package org.springframework.context.event.listener;

import org.springframework.context.event.event.ApplicationEvent;

import java.util.EventListener;

/**
 * 事件监听器
 *
 * @param <E> 事件类型
 */
public interface ApplicationListener<E extends ApplicationEvent> extends EventListener {

    void onApplicationEvent(E event);
}