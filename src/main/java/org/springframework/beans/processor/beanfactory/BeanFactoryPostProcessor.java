package org.springframework.beans.processor.beanfactory;

import org.springframework.beans.DefaultListableBeanFactory;
import org.springframework.core.exception.BeansException;

/**
 * BeanFactoryPostProcessor扩展点，主要作用对象是beanFactory，所以你看到它的入参都是BeanFactory类型。
 * BeanPostProcessor要处理Bean我能理解，毕竟Bean有生命周期、还可能需要创建代理对象。
 * 但是BeanFactory有啥好处理的呢？可多了。
 * 比如，在容器启动时虽然会加载BeanDefinition，但这些BeanDefinition可能还不完整，比如value="${foo}"这种占位符，
 * 此时就需要BeanFactoryPostProcessor来处理了（在Bean创建前把不确定的东西都处理好）。
 * 又比如，@EnableAspectJAutoProxy开启AspectJ AOP，
 * 实际上是通过@Import导入AspectJAutoProxyRegistrar并注册AnnotationAwareAspectJAutoProxyCreator
 * 你知道是谁在背后支持这个机制吗？是ConfigurationClassPostProcessor。它其实也是一个BeanFactoryPostProcessor。
 *
 * 简单来说，在Bean正式开始创建之前，可能有一系列准备程序，此时都可以交给BeanFactoryPostProcessor来处理。
 */
public interface BeanFactoryPostProcessor {

    /**
     * 此时所有BeanDefinition加载完毕但未实例化，允许在此修改BeanDefinition
     */
    void postProcessBeanFactory(DefaultListableBeanFactory beanFactory) throws BeansException;

}
