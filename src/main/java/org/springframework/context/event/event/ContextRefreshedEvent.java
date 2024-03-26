package org.springframework.context.event.event;

import org.springframework.context.ApplicationContext;

/**
 * 容器刷新完成事件
 */
public class ContextRefreshedEvent extends ApplicationContextEvent {

	public ContextRefreshedEvent(ApplicationContext source) {
		super(source);
	}
}
