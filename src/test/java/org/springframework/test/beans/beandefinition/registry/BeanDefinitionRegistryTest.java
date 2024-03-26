package org.springframework.test.beans.beandefinition.registry;


import org.junit.Test;
import org.springframework.beans.DefaultListableBeanFactory;
import org.springframework.beans.beandefinition.definition.BeanDefinition;
import org.springframework.beans.beandefinition.registry.BeanDefinitionRegistry;

/**
 * {@link BeanDefinition}是Spring的一个核心概念，用于指导Spring创建Bean。
 * Spring通过加载XML或者扫描指定路径下的Components来生成BeanDefinition，并将它们注册到BeanDefinitionRegistry中。
 * <p>
 * 简单来讲，你可以将BeanDefinition看做食品原材料，BeanDefinitionRegistry就是原材料仓库。
 * BeanFactory从BeanDefinitionRegistry中获取BeanDefinition，并根据这些BeanDefinition来创建Bean（真正可食用的食品）。
 */
public class BeanDefinitionRegistryTest {

    @Test
    public void testBeanDefinitionRegistry() {
        // 存储原材料，等待后续加工
        BeanDefinitionRegistry beanDefinitionRegistry = new DefaultListableBeanFactory();
        beanDefinitionRegistry.registerBeanDefinition("mockBean1", new BeanDefinition(MockBean1.class));
        beanDefinitionRegistry.registerBeanDefinition("mockBean2", new BeanDefinition(MockBean2.class));
        for (String dbName : beanDefinitionRegistry.getBeanDefinitionNames()) {
            System.out.println(dbName + "=>" + beanDefinitionRegistry.getBeanDefinition(dbName));
        }
    }

    public static class MockBean1 {

    }

    public static class MockBean2 {

    }
}
