package org.springframework.beans.processor.beanfactory;

import org.springframework.beans.DefaultListableBeanFactory;
import org.springframework.beans.beandefinition.definition.BeanDefinition;
import org.springframework.beans.beandefinition.definition.PropertyValue;
import org.springframework.beans.beandefinition.definition.PropertyValues;
import org.springframework.core.StringValueResolver;
import org.springframework.core.exception.BeansException;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Properties;

/**
 * BeanFactoryPostProcessor的一个扩展实现，用于加载properties文件配置
 * <p>
 * 在Spring中这个类已经废弃，被PropertyPlaceholderBeanDefinitionParser取代。
 * 基于XML开发时，可以使用<context:property-placeholder>标签来加载属性文件，本质上就是注册一个
 * PropertyPlaceholderBeanDefinitionParser（详见ContextNamespaceHandler）
 */
public class PropertyPlaceholderConfigurer implements BeanFactoryPostProcessor {

    public static final String PLACEHOLDER_PREFIX = "${";

    public static final String PLACEHOLDER_SUFFIX = "}";

    /**
     * properties文件路径
     */
    private String location;

    @Override
    public void postProcessBeanFactory(DefaultListableBeanFactory beanFactory) throws BeansException {
        // 加载指定的properties配置文件
        Properties properties = loadProperties();

        // 遍历BeanDefinition，将BeanDefinition.property中的${}占位符替换成实际值，比如 author=${author} 替换成 author=Rod Johnson
        processProperties(beanFactory, properties);
        
        // 上面的功能牛逼吧？
        // 但当前方法仅在ApplicationContext#refresh时执行一次，后面想用也没法用了
        // 所以，Spring把整个解析逻辑封装到PlaceholderResolvingStringValueResolver（内部类），注册到BeanFactory中，后续想用就可以用
        StringValueResolver valueResolver = new PlaceholderResolvingStringValueResolver(properties);
        beanFactory.addEmbeddedValueResolver(valueResolver);
    }

    private void processProperties(DefaultListableBeanFactory beanFactory, Properties properties) {
        String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);
            resolvePropertyValues(beanDefinition, properties);
        }
    }

    private void resolvePropertyValues(BeanDefinition beanDefinition, Properties properties) {
        PropertyValues propertyValues = beanDefinition.getPropertyValues();
        for (PropertyValue propertyValue : propertyValues.getPropertyValues()) {
            Object value = propertyValue.getValue();
            if (value instanceof String) {
                value = resolvePlaceholders((String)value, properties);
                propertyValues.addPropertyValue(new PropertyValue(propertyValue.getName(), value));
            }
        }
    }

    /**
     * 先解析属性值中的占位符：比如解析${author}得到author 再把author作为key，从properties配置中获取对应的值 最后返回真实的value
     */
    private Object resolvePlaceholders(String value, Properties properties) {
        StringBuilder buf = new StringBuilder(value);
        int startIndex = value.indexOf(PLACEHOLDER_PREFIX);
        int endIndex = value.indexOf(PLACEHOLDER_SUFFIX);
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            String placeholder = value.substring(startIndex + PLACEHOLDER_PREFIX.length(), endIndex);
            String propVal = properties.getProperty(placeholder);
            buf.replace(startIndex, endIndex + PLACEHOLDER_SUFFIX.length(), propVal);
        }
        return buf.toString();
    }

    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * 加载属性配置文件
     */
    private Properties loadProperties() {
        try {
            DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
            Resource resource = resourceLoader.getResource(location);
            Properties properties = new Properties();
            properties.load(resource.getInputStream());
            return properties;
        } catch (IOException e) {
            throw new BeansException("Could not load properties", e);
        }
    }

    /**
     * 将PropertyPlaceholderConfigurer.this.resolvePlaceholder的逻辑封装成一个StringValueResolver
     */
    private class PlaceholderResolvingStringValueResolver implements StringValueResolver {

        private final Properties properties;

        public PlaceholderResolvingStringValueResolver(Properties properties) {
            this.properties = properties;
        }

        public String resolveStringValue(String strVal) throws BeansException {
            return PropertyPlaceholderConfigurer.this.resolvePlaceholder(strVal, properties);
        }
    }

    /**
     * 先解析属性值中的占位符：比如解析${author}得到author
     * 再把author作为key，从properties配置中获取对应的值
     * 最后返回真实的value
     */
    private String resolvePlaceholder(String value, Properties properties) {
        StringBuilder buf = new StringBuilder(value);
        int startIndex = value.indexOf(PLACEHOLDER_PREFIX);
        int endIndex = value.indexOf(PLACEHOLDER_SUFFIX);
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            String propKey = value.substring(startIndex + 2, endIndex);
            String propVal = properties.getProperty(propKey);
            buf.replace(startIndex, endIndex + 1, propVal);
        }
        return buf.toString();
    }
}
