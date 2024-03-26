package org.springframework.beans.processor.bean;

import org.springframework.beans.beandefinition.definition.PropertyValues;
import org.springframework.core.common.Nullable;
import org.springframework.core.exception.BeansException;

/**
 * BeanPostProcessor扩展，额外关注【bean实例化】前后的操作，以及Bean属性操作
 */
public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {

    /**
     * 在bean实例化之前执行
     */
    @Nullable
    default Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        return null;
    }

    /**
     * bean实例化之后，设置属性之前执行
     */
    default boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        return true;
    }

    /**
     * bean实例化之后，设置属性之前执行
     */
    @Nullable
    default PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {
        return null;
    }

}
