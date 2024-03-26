package org.springframework.test.beans.beanfactory;


import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.BeanFactory;
import org.springframework.beans.DefaultListableBeanFactory;
import org.springframework.beans.SingletonBeanRegistry;
import org.springframework.beans.beandefinition.definition.BeanDefinition;
import org.springframework.beans.beandefinition.registry.BeanDefinitionRegistry;
import org.springframework.beans.lifecycle.DisposableBean;
import org.springframework.beans.lifecycle.InitializingBean;

/**
 * Spring中最重要的一个组件是{@link DefaultListableBeanFactory}，你能想到绝大部分功能都是它实现或组合的。
 * DefaultListableBeanFactory有三个最重要的角色（一个接口代表一个角色，也代表拥有某项能力）：
 * - BeanDefinitionRegistry（具备注册BeanDefinition的能力）
 * - BeanFactory（具备获取Bean、创建Bean的能力）
 * - SingletonBeanRegistry（具备存储SingletonBean的能力）
 */
public class BeanFactoryTest {

    @Test
    public void testBeanFactoryContainer() {
        BeanFactory beanFactory = new DefaultListableBeanFactory();
        // beanFactory.getBean() BeanFactory只有get方法，那么创建出来的Bean存放在哪呢？

        // 答案是SingletonBeanRegistry（单例注册表）

        // DefaultListableBeanFactory 实现了 BeanDefinitionRegistry, BeanFactory, SingletonBeanRegistry
        // 作为 BeanDefinitionRegistry ，它需要存储 BeanDefinition
        // 作为 SingletonBeanRegistry ， 它需要存储 SingletonBean（PrototypeBean无需存储，需要时重复创建）
        // 作为 BeanFactory， 它提供 Bean

        // 接下来，我们借助 DefaultListableBeanFactory 的三种角色，来完成Bean的创建与获取

        // 1. 存储 BeanDefinition
        ((BeanDefinitionRegistry) beanFactory).registerBeanDefinition("mySingletonBean", new BeanDefinition(MySingletonBean.class));
        Assert.assertTrue(((BeanDefinitionRegistry) beanFactory).containsBeanDefinition("mySingletonBean"));
        // 2. 先看看单例池中是否已经存在Bean
        Object mySingletonBean = ((SingletonBeanRegistry) beanFactory).getSingleton("mySingletonBean");
        Assert.assertNull(mySingletonBean);
        // 3. 通过BeanFactory创建并获取 Bean（get时如果单例池中没有，则创建）
        mySingletonBean = beanFactory.getBean("mySingletonBean");
        Assert.assertNotNull(mySingletonBean);
    }

    @Test
    public void testBeanLifecycle() {
        // Spring为托管的Bean设计了完整的生命周期：实例化、属性赋值、初始化、销毁（容器关闭或启动异常时）
        // 也就是说，无论是哪个类（Person、Department），只要交给Spring管理，都要经历以上步骤
        // 对于这条必经之路，Spring提供了一些生命周期扩展点，允许我们在Bean的各个阶段执行一些自定义逻辑
        // 比如：
        // 实现InitializingBean、DisposableBean（Spring会在 初始化、销毁 时触发接口方法）：public class Person implements InitializingBean, DisposableBean
        // 指定initMethodName、destroyMethodName（Spring会在 初始化、销毁 时触发你指定的方法）：<bean id="person" class="...Person" init-method="xxx" destroy-method="yyy">
        // 注解@PreDestroy、@PostConstruct

        // 这些方式本质上就是一个扩展点，让外部有机会参与到整个Bean的创建过程中来。

        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        // 注册BeanDefinition
        beanFactory.registerBeanDefinition("myLifeCycleBean", constructLifeCycleBeanDefinition(MyLifeCycleBean.class));
        Assert.assertFalse(beanFactory.containsSingleton("myLifeCycleBean"));
        // getBean会先从单例池获取，如果单例池中不存在，则根据已注册的BeanDefinition信息创建Bean，然后缓存到单例池
        beanFactory.getBean("myLifeCycleBean", MyLifeCycleBean.class);
        Assert.assertTrue(beanFactory.containsSingleton("myLifeCycleBean"));
        // 销毁单例bean
        beanFactory.destroySingletons();
        Assert.assertFalse(beanFactory.containsSingleton("myLifeCycleBean"));
    }


    public static class MySingletonBean {

    }

    public static class MyLifeCycleBean implements InitializingBean, DisposableBean {

        @Override
        public void afterPropertiesSet() throws Exception {
            System.out.println("InitializingBean#afterPropertiesSet被调用");
        }

        @Override
        public void destroy() throws Exception {
            System.out.println("DisposableBean#destroy被调用");
        }

        public void customInit() {
            System.out.println("init-method#customInit被调用");
        }

        public void customDestroy() {
            System.out.println("destroy-method#customDestroy被调用");
        }
    }

    private BeanDefinition constructLifeCycleBeanDefinition(Class<?> clazz) {
        BeanDefinition beanDefinition = new BeanDefinition(clazz);
        beanDefinition.setInitMethodName("customInit");
        beanDefinition.setDestroyMethodName("customDestroy");
        return beanDefinition;
    }

}
