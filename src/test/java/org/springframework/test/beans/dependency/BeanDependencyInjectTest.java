package org.springframework.test.beans.dependency;


import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.DefaultListableBeanFactory;
import org.springframework.beans.annotation.Autowired;
import org.springframework.beans.annotation.Value;
import org.springframework.beans.beandefinition.reader.XmlBeanDefinitionReader;
import org.springframework.beans.processor.bean.BeanPostProcessor;
import org.springframework.beans.processor.beanfactory.BeanFactoryPostProcessor;
import org.springframework.beans.processor.beanfactory.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Component;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.beans.dependency.autowire.Company;
import org.springframework.test.beans.dependency.autowire.Department;
import org.springframework.test.beans.dependency.autowire.Employee;

import java.util.Map;

/**
 * dummy-ioc实现了老式的autowire和最常用的@Autowired注解。暂不支持循环依赖，Spring也不推荐循环依赖。
 * 通过这个测试案例，你会了解到BeanFactory虽然强大，但各个组件之间配合起来却非常复杂。
 * 为了降低BeanFactory的使用成本，以及提供更强大的功能（比如事件机制、国际化等），Spring引入了ApplicationContext的概念。
 */
public class BeanDependencyInjectTest {

    /**
     * XML时代的产物，基本被@Autowired和@Resource注解取代
     */
    @Test
    public void testAutowireMode() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory, resourceLoader);

        // loadBeanDefinitions：加载XML、解析XML、注册BeanDefinition
        reader.loadBeanDefinitions("classpath:3_autowire_mode.xml");

        // 注意！！如果把这个注释了，Company中的${companyName}就无法解析了
        PropertyPlaceholderConfigurer placeholderConfigurer = beanFactory.getBean("placeholderConfigurer", PropertyPlaceholderConfigurer.class);
        Assert.assertNotNull(placeholderConfigurer);
        placeholderConfigurer.postProcessBeanFactory(beanFactory);

        Company company = beanFactory.getBean("company", Company.class);
        Department department = beanFactory.getBean("department", Department.class);
        Employee employee = beanFactory.getBean("employee", Employee.class);
        Assert.assertNotNull(company);
        Assert.assertEquals("org.spring", company.getName());
        Assert.assertNotNull(department);
        Assert.assertEquals(company.getDepartment(), department);
        Assert.assertNull(department.getEmployee()); // 因为Department没有开启autowireByName
    }

    /**
     * 你会发现，仅仅拥有BeanFactory、BeanPostProcessor、BeanFactoryPostProcessor、
     * XmlBeanDefinitionReader这些零碎的组件还不够，使用起来很麻烦，需要我们了解它们之间的关系并熟悉装配步骤，否则根本无法顺利创建Bean
     * 难道没有别的办法了吗？
     * <p>
     * 当然有，Spring提供了一个{@link ApplicationContext}接口，它继承了BeanFactory接口，
     * 并且在此基础上添加了其他功能，比如国际化、资源访问、事件传播等。
     * 当然，使用ApplicationContext最重要的原因是，它是个比BeanFactory好用很多的大黑盒，我们不需要了解太多细节。
     * 因此，我们通常使用ApplicationContext来替代BeanFactory。
     */
    @Test
    public void testAutowired() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory, resourceLoader);

        // 加载XML，开始扫描@Component，并注册AutowiredAnnotationBeanPostProcessor（BeanDefinition）
        reader.loadBeanDefinitions("classpath:4_autowired_value.xml");

        // 向BeanFactory注册PropertyPlaceholderConfigurer，用来解析${}占位符
        invokeBeanFactoryPostProcessors(beanFactory);

        // 必须把BeanPostProcessor都先注册一遍（先实例化BeanPostProcessor），不然它们无法对后续的Bean进行处理
        registerBeanPostProcessors(beanFactory);

        School school = beanFactory.getBean("school", School.class);
        Student student = beanFactory.getBean("student", Student.class);
        Assert.assertNotNull(school);
        Assert.assertNotNull(student);
        Assert.assertEquals(school.getStudent(), student);
        Assert.assertEquals("Peeking University", school.getName());
        Assert.assertEquals("Rod Johnson", student.getName());
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

    @Component
    public static class School {
        @Value("${schoolName}")
        private String name;

        @Autowired
        private Student student;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Student getStudent() {
            return student;
        }

        public void setStudent(Student student) {
            this.student = student;
        }
    }

    @Component
    public static class Student {
        @Value("${studentName}")
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
