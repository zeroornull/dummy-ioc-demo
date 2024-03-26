package org.springframework.context.event.multicaster;

import org.springframework.core.common.Nullable;
import org.springframework.beans.BeanFactory;
import org.springframework.context.event.event.ApplicationEvent;
import org.springframework.context.event.listener.ApplicationListener;

import java.util.Collection;
import java.util.concurrent.Executor;


public class SimpleApplicationEventMulticaster extends AbstractApplicationEventMulticaster {

	@Nullable
	private Executor taskExecutor;

	public SimpleApplicationEventMulticaster(BeanFactory beanFactory) {
		setBeanFactory(beanFactory);
	}

	@Override
	public void multicastEvent(ApplicationEvent event) {
		Collection<ApplicationListener<?>> matchedListeners = getApplicationListeners(event);
		for (ApplicationListener<?> listener : matchedListeners) {
			Executor executor = getTaskExecutor();
			if (executor != null) {
				// 如果外界设置了线程池，则变为异步事件
				executor.execute(() -> invokeListener(listener, event));
			} else {
				// 默认同步事件
				invokeListener(listener, event);
			}
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private void invokeListener(ApplicationListener listener, ApplicationEvent event) {
		listener.onApplicationEvent(event);
	}

	@Nullable
	protected Executor getTaskExecutor() {
		return this.taskExecutor;
	}

	public void setTaskExecutor(@Nullable Executor executor) {
		this.taskExecutor = executor;
	}
}
