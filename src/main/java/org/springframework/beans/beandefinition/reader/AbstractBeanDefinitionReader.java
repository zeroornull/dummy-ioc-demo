package org.springframework.beans.beandefinition.reader;

import org.springframework.beans.beandefinition.registry.BeanDefinitionRegistry;
import org.springframework.core.exception.BeansException;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

/**
 * 抽象BeanDefinitionReader
 */
public abstract class AbstractBeanDefinitionReader implements BeanDefinitionReader {

	// 资源加载器
	private ResourceLoader resourceLoader;

	// 组装好BeanDefinition后，注册到BeanDefinitionRegistry
	private final BeanDefinitionRegistry registry;

	protected AbstractBeanDefinitionReader(BeanDefinitionRegistry registry) {
		// 不指定的话，使用默认的DefaultResourceLoader
		this(registry, new DefaultResourceLoader());
	}

	public AbstractBeanDefinitionReader(BeanDefinitionRegistry registry, ResourceLoader resourceLoader) {
		this.registry = registry;
		this.resourceLoader = resourceLoader;
	}

	@Override
	public BeanDefinitionRegistry getRegistry() {
		return registry;
	}

	@Override
	public void loadBeanDefinitions(String[] locations) throws BeansException {
		for (String location : locations) {
			loadBeanDefinitions(location);
		}
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Override
	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}
}
