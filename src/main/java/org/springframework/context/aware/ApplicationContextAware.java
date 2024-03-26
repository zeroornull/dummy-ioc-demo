package org.springframework.context.aware;

import org.springframework.beans.aware.Aware;
import org.springframework.context.ApplicationContext;
import org.springframework.core.exception.BeansException;

/**
 * 实现该接口，能感知所属ApplicationContext
 */
public interface ApplicationContextAware extends Aware {

	void setApplicationContext(ApplicationContext applicationContext) throws BeansException;
}
