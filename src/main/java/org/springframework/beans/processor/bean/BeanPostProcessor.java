package org.springframework.beans.processor.bean;

import org.springframework.core.common.Nullable;
import org.springframework.core.exception.BeansException;

/**
 * BeanPostProcessor扩展点，主要作用对象是bean。
 * BeanPostProcessor最出名的一个实现类是AbstractAutoProxyCreator，被用于AOP创建代理对象。
 * dummy-ioc目前还未支持AOP。
 */
public interface BeanPostProcessor {

    /**
     * 在bean执行初始化方法之前执行此方法
     */
    @Nullable
    default Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * 在bean执行初始化方法之后执行此方法
     */
    @Nullable
    default Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

}
