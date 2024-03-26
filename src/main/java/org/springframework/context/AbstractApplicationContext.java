package org.springframework.context;

import org.springframework.beans.DefaultListableBeanFactory;
import org.springframework.beans.processor.bean.BeanPostProcessor;
import org.springframework.beans.processor.beanfactory.BeanFactoryPostProcessor;
import org.springframework.context.aware.ApplicationContextAwareProcessor;
import org.springframework.context.event.event.ApplicationEvent;
import org.springframework.context.event.event.ContextRefreshedEvent;
import org.springframework.context.event.listener.ApplicationListener;
import org.springframework.context.event.multicaster.ApplicationEventMulticaster;
import org.springframework.context.event.multicaster.SimpleApplicationEventMulticaster;
import org.springframework.core.exception.BeansException;
import org.springframework.core.io.DefaultResourceLoader;

import java.util.Collection;
import java.util.Map;

/**
 * 抽象ApplicationContext，模板方法模式，规划了整个容器的启动流程。
 */
public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ApplicationContext {

    public static final String APPLICATION_EVENT_MULTICASTER_BEAN_NAME = "applicationEventMulticaster";

    private ApplicationEventMulticaster applicationEventMulticaster;

    @Override
    public void refresh() throws BeansException {
        // 创建BeanFactory，并加载BeanDefinition
        DefaultListableBeanFactory beanFactory = obtainFreshBeanFactory();

        // 做一些准备工作
        prepareBeanFactory(beanFactory);

        try {
            // 【扩展点】执行BeanFactoryPostProcessor：在实例化Bean之前，允许BFP修改BeanDefinition
            invokeBeanFactoryPostProcessors(beanFactory);

            // 【扩展点】注册BeanPostProcessor：创建BP并加入单例池，后续可以通过BP干预Bean的创建过程
            registerBeanPostProcessors(beanFactory);

            // 初始化事件发布者
            initApplicationEventMulticaster();

            // 注册事件监听器
            registerListeners();

            // 完成Bean的初始化
            finishBeanFactoryInitialization(beanFactory);

            // 发布容器刷新完成事件
            finishRefresh();
        } catch (BeansException ex) {
            // 创建过程中发生异常，销毁已创建的单例bean，并执行destroy生命周期钩子
            destroyBeans();
            throw ex;
        }
    }

    protected DefaultListableBeanFactory obtainFreshBeanFactory() {
        refreshBeanFactory();
        return getBeanFactory();
    }

    protected abstract void refreshBeanFactory() throws BeansException;

    public abstract DefaultListableBeanFactory getBeanFactory();

    protected void prepareBeanFactory(DefaultListableBeanFactory beanFactory) {
        // 这里先添加ApplicationContextAwareProcessor，后续用于处理继承自ApplicationContextAware的bean（为其注入applicationContext）
        beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));
        // 省略其他工作...
    }

    protected void finishBeanFactoryInitialization(DefaultListableBeanFactory beanFactory) {
        beanFactory.preInstantiateSingletons();
    }

    protected void invokeBeanFactoryPostProcessors(DefaultListableBeanFactory beanFactory) {
        // 这里会执行一个叫PropertyPlaceholderConfigurer的BeanFactoryPostProcessor，负责把${}占位符替换成实际的value
        Map<String, BeanFactoryPostProcessor> beanFactoryPostProcessorMap = beanFactory.getBeansOfType(BeanFactoryPostProcessor.class);
        for (BeanFactoryPostProcessor beanFactoryPostProcessor : beanFactoryPostProcessorMap.values()) {
            beanFactoryPostProcessor.postProcessBeanFactory(beanFactory);
        }
    }

    protected void registerBeanPostProcessors(DefaultListableBeanFactory beanFactory) {
        // 获取对应类型的BeanPostProcessor（如果未创建，则createBean，并把bp加入到singletonObjects）
        Map<String, BeanPostProcessor> beanPostProcessorMap = beanFactory.getBeansOfType(BeanPostProcessor.class);
        for (BeanPostProcessor beanPostProcessor : beanPostProcessorMap.values()) {
            beanFactory.addBeanPostProcessor(beanPostProcessor);
        }
    }

    /**
     * 初始化事件发布者
     */
    protected void initApplicationEventMulticaster() {
        DefaultListableBeanFactory beanFactory = getBeanFactory();
        applicationEventMulticaster = new SimpleApplicationEventMulticaster(beanFactory);
        beanFactory.addSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, applicationEventMulticaster);
    }

    /**
     * 注册事件监听器
     */
    @SuppressWarnings("rawtypes")
    protected void registerListeners() {
        Collection<ApplicationListener> applicationListeners = getBeansOfType(ApplicationListener.class).values();
        for (ApplicationListener applicationListener : applicationListeners) {
            applicationEventMulticaster.addApplicationListener(applicationListener);
        }
    }

    /**
     * 发布容器刷新完成事件
     */
    protected void finishRefresh() {
        publishEvent(new ContextRefreshedEvent(this));
    }

    @Override
    public void publishEvent(ApplicationEvent event) {
        applicationEventMulticaster.multicastEvent(event);
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return getBeanFactory().getBean(name, requiredType);
    }

    public <T> T getBean(Class<T> requiredType) throws BeansException {
        return getBeanFactory().getBean(requiredType);
    }

    @Override
    public Object getBean(String name) throws BeansException {
        return getBeanFactory().getBean(name);
    }

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
        return getBeanFactory().getBeansOfType(type);
    }

    public void close() {
        doClose();
    }

    public void registerShutdownHook() {
        Thread shutdownHook = new Thread() {
            public void run() {
                doClose();
            }
        };
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    protected void doClose() {
        //执行单例bean的销毁方法
        destroyBeans();
    }

    protected void destroyBeans() {
        getBeanFactory().destroySingletons();
    }
}
