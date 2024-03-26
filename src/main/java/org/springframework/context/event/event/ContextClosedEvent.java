package org.springframework.context.event.event;

import org.springframework.context.ApplicationContext;

/**
 * 容器关闭事件
 */
public class ContextClosedEvent extends ApplicationContextEvent {

	public ContextClosedEvent(ApplicationContext source) {
		super(source);
	}
}
