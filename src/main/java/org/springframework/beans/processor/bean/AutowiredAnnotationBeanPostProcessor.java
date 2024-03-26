package org.springframework.beans.processor.bean;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.BeanFactory;
import org.springframework.beans.DefaultListableBeanFactory;
import org.springframework.beans.annotation.Autowired;
import org.springframework.beans.annotation.Qualifier;
import org.springframework.beans.annotation.Value;
import org.springframework.beans.aware.BeanFactoryAware;
import org.springframework.beans.beandefinition.definition.PropertyValue;
import org.springframework.beans.beandefinition.definition.PropertyValues;
import org.springframework.core.exception.BeansException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * 处理@Autowired和@Value注解的BeanPostProcessor
 * Spring原版处理方式十分复杂，支持的autowiredAnnotationTypes详见AutowiredAnnotationBeanPostProcessor无参构造
 */
public class AutowiredAnnotationBeanPostProcessor implements InstantiationAwareBeanPostProcessor, BeanFactoryAware {

    private DefaultListableBeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {

        Class<?> clazz = bean.getClass();
        Field[] fields = clazz.getDeclaredFields();

        // 处理@Value注解
        try {
            injectByValue(bean, fields);
        } catch (Exception ex) {
            throw new BeansException("Error setting property values for bean: " + beanName, ex);
        }

        // 处理@Autowired注解
        try {
            injectByAutowired(bean, fields, pvs);
        } catch (Exception ex) {
            throw new BeansException("Error setting property values for bean: " + beanName, ex);
        }

        return pvs;
    }

    private void injectByValue(Object bean, Field[] fields) throws InvocationTargetException, IllegalAccessException {
        for (Field field : fields) {
            Value valueAnnotation = field.getAnnotation(Value.class);
            if (valueAnnotation != null) {
                String value = valueAnnotation.value();
                value = beanFactory.resolveEmbeddedValue(value);
                BeanUtils.setProperty(bean, field.getName(), value);
            }
        }
    }

    private void injectByAutowired(Object bean, Field[] fields, PropertyValues pvs) throws InvocationTargetException, IllegalAccessException {
        for (Field field : fields) {
            Autowired autowiredAnnotation = field.getAnnotation(Autowired.class);
            if (autowiredAnnotation != null) {
                Class<?> fieldType = field.getType();
                Qualifier qualifierAnnotation = field.getAnnotation(Qualifier.class);
                Object dependentBean;
                if (qualifierAnnotation != null) {
                    String dependentBeanName = qualifierAnnotation.value();
                    dependentBean = beanFactory.getBean(dependentBeanName, fieldType);
                } else {
                    dependentBean = beanFactory.getBean(fieldType);
                }

                BeanUtils.setProperty(bean, field.getName(), dependentBean);
                pvs.addPropertyValue(new PropertyValue(field.getName(), dependentBean));
            }
        }
    }

    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        return null;
    }

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        return true;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return null;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return null;
    }
}
