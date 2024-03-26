package org.springframework.beans.beandefinition.registry;


import org.springframework.beans.BeanFactory;
import org.springframework.beans.SingletonBeanRegistry;
import org.springframework.beans.beandefinition.definition.BeanDefinition;
import org.springframework.core.exception.BeansException;

/**
 * BeanDefinition注册表接口，用于存取BeanDefinition
 * <p>
 * 在介绍BeanDefinition时，dummy-ioc拆分了3个package：
 * - definition
 * - reader
 * - registry
 * 正好代表了 定义BeanDefinition、读取BeanBeanDefinition、注册BeanDefinition 3个步骤
 * BeanDefinition就像原材料，而BeanDefinitionRegistry则是原材料仓库。
 * 有了原材料，就可以开始加工，生成真正的可食用的美味食物了。
 * @see BeanFactory
 * @see SingletonBeanRegistry
 */
public interface BeanDefinitionRegistry {

    /**
     * 向注册表中注BeanDefinition
     */
    void registerBeanDefinition(String beanName, BeanDefinition beanDefinition);

    /**
     * 根据名称查找BeanDefinition
     */
    BeanDefinition getBeanDefinition(String beanName) throws BeansException;

    /**
     * 是否包含指定名称的BeanDefinition
     */
    boolean containsBeanDefinition(String beanName);

    /**
     * 返回定义的所有bean的名称
     */
    String[] getBeanDefinitionNames();
}