package org.springframework.context.event.event;

import org.springframework.context.ApplicationContext;

/**
 * 容器事件
 */
public abstract class ApplicationContextEvent extends ApplicationEvent {

	public ApplicationContextEvent(ApplicationContext source) {
		super(source);
	}

	public final ApplicationContext getApplicationContext() {
		return (ApplicationContext) getSource();
	}
}
