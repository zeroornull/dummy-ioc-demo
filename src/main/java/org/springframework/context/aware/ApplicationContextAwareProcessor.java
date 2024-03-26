package org.springframework.context.aware;

import org.springframework.beans.processor.bean.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.exception.BeansException;

/**
 * BeanPostProcessor：为实现了ApplicationContextAware接口的bean提供Spring容器的引用。
 */
public class ApplicationContextAwareProcessor implements BeanPostProcessor {

	private final ApplicationContext applicationContext;

	public ApplicationContextAwareProcessor(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof ApplicationContextAware) {
			// 注入applicationContext
			((ApplicationContextAware) bean).setApplicationContext(applicationContext);
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}
}
