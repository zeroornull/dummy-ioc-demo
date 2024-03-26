package org.springframework.test.beans.beandefinition;


import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.DefaultListableBeanFactory;
import org.springframework.beans.beandefinition.definition.BeanDefinition;
import org.springframework.beans.beandefinition.definition.PropertyValues;
import org.springframework.beans.beandefinition.reader.XmlBeanDefinitionReader;
import org.springframework.beans.beandefinition.registry.BeanDefinitionRegistry;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

/**
 * ResourceLoader：加载资源
 * BeanDefinitionRegistry：存取BeanDefinition
 * XmlBeanDefinitionReader = ResourceLoader + BeanDefinitionRegistry
 * - 先加载XML到内存（ResourceLoader）
 * - 再将XML内容解析成BeanDefinition（dom4j)
 * - 最后将BeanDefinition注册到BeanDefinitionRegistry
 */
public class BeanDefinitionReaderTest {

    @Test
    public void testReadBeanDefinitionFromXml() {
        BeanDefinitionRegistry registry = new DefaultListableBeanFactory();
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(registry, resourceLoader);

        // loadBeanDefinitions：加载XML、解析XML、注册BeanDefinition
        reader.loadBeanDefinitions("classpath:1_bean-definition-reader-xml.xml");
        BeanDefinition person = registry.getBeanDefinition("person");
        Assert.assertNotNull(person);

        PropertyValues propertyValues = person.getPropertyValues();
        Assert.assertEquals("bravo", propertyValues.getPropertyValue("name").getValue());
        Assert.assertEquals("18", propertyValues.getPropertyValue("age").getValue());
    }

    @Test
    public void testReadBeanDefinitionFromComponent() {
        BeanDefinitionRegistry registry = new DefaultListableBeanFactory();
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(registry, resourceLoader);

        // loadBeanDefinitions：加载XML、解析XML（如果有component-scan标签，则额外扫描@Component组件）、注册BeanDefinition
        reader.loadBeanDefinitions("classpath:2_bean-definition-reader-component.xml");
        BeanDefinition person = registry.getBeanDefinition("person");
        Assert.assertNotNull(person);
    }
}
