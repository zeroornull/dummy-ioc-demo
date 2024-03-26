package org.springframework.context;

import org.springframework.beans.DefaultListableBeanFactory;
import org.springframework.beans.beandefinition.reader.XmlBeanDefinitionReader;
import org.springframework.core.exception.BeansException;

/**
 * xml文件的应用上下文
 */
public class ClassPathXmlApplicationContext extends AbstractApplicationContext {

    private final String[] configLocations;

    private DefaultListableBeanFactory beanFactory;

    public ClassPathXmlApplicationContext(String configLocation) throws BeansException {
        this(new String[]{configLocation});
    }

    public ClassPathXmlApplicationContext(String[] configLocations) throws BeansException {
        this.configLocations = configLocations;
        refresh();
    }

    @Override
    protected void refreshBeanFactory() throws BeansException {
        // 创建BeanFactory
        DefaultListableBeanFactory beanFactory = createBeanFactory();
        // 将BeanDefinition加载到BeanFactory
        loadBeanDefinitions(beanFactory);
        // 赋值
        this.beanFactory = beanFactory;
    }

    @Override
    public DefaultListableBeanFactory getBeanFactory() {
        return beanFactory;
    }

    private DefaultListableBeanFactory createBeanFactory() {
        return new DefaultListableBeanFactory();
    }

    private void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) {
        // 创建XmlBeanDefinitionReader
        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory, this);
        if (configLocations != null) {
            // 将XML中的bean标签解析成BeanDefinition对象
            beanDefinitionReader.loadBeanDefinitions(configLocations);
        }
    }

}
