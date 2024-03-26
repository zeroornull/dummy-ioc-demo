package org.springframework.beans;


import org.springframework.core.exception.BeansException;
import org.springframework.beans.beandefinition.registry.*;

import java.util.Map;

/**
 * Spring核心组件之一，负责创建Bean、管理Bean（让其经历生命周期及扩展点）以及装配Bean
 * BeanFactory本身不具备Bean的存储能力，需要借助{@link SingletonBeanRegistry}来存储。
 * 不过在实际看源码时，你可能感受不深，因为{@link DefaultListableBeanFactory}是一个多面手，同时实现了以下接口。
 * - {@link BeanDefinitionRegistry} 加载并存储BeanDefinition（下游原材料供应商、原材料仓库）
 * - {@link BeanFactory}            根据BeanDefinition创建Bean（食品加工厂）
 * - {@link SingletonBeanRegistry}  存储单例Bean（食品仓库）
 */
public interface BeanFactory {

    Object getBean(String name) throws BeansException;


    <T> T getBean(Class<T> requiredType) throws BeansException;


    <T> T getBean(String name, Class<T> requiredType) throws BeansException;

    <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException;

}
