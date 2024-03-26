package org.springframework.beans.beandefinition.reader;

import org.springframework.beans.beandefinition.registry.BeanDefinitionRegistry;
import org.springframework.core.exception.BeansException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * BeanDefinition读取器：
 * 1.依赖ResourceLoader进行文件/资源读取
 * 2.解析并组装BeanDefinition
 * 3.把BeanDefinition注册到BeanDefinitionRegistry
 */
public interface BeanDefinitionReader {

    BeanDefinitionRegistry getRegistry();

    ResourceLoader getResourceLoader();

    void loadBeanDefinitions(Resource resource) throws BeansException;

    void loadBeanDefinitions(String location) throws BeansException;

    void loadBeanDefinitions(String[] locations) throws BeansException;
}
