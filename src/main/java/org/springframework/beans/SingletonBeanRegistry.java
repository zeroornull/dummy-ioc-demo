package org.springframework.beans;

/**
 * 单例bean注册表接口
 * 在Spring中，只有SingletonBean创建后需要缓存，下次调用BeanFactory#getBean时会优先向SingletonBeanRegistry获取。
 * 至于PrototypeBean，每次调用BeanFactory#getBean都会重新创建，Spring只负责生产、不负责存储。
 */
public interface SingletonBeanRegistry {

    Object getSingleton(String beanName);

    void addSingleton(String beanName, Object singletonObject);

    boolean containsSingleton(String beanName);
}
