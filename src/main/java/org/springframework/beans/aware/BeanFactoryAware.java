package org.springframework.beans.aware;

import org.springframework.beans.BeanFactory;
import org.springframework.core.exception.BeansException;

/**
 * 实现该接口，能感知所属BeanFactory
 */
public interface BeanFactoryAware extends Aware {

	void setBeanFactory(BeanFactory beanFactory) throws BeansException;

}
