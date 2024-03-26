package org.springframework.beans;


import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.aware.Aware;
import org.springframework.beans.aware.BeanFactoryAware;
import org.springframework.beans.beandefinition.definition.BeanDefinition;
import org.springframework.beans.beandefinition.definition.BeanReference;
import org.springframework.beans.beandefinition.definition.PropertyValue;
import org.springframework.beans.beandefinition.definition.PropertyValues;
import org.springframework.beans.beandefinition.registry.BeanDefinitionRegistry;
import org.springframework.beans.lifecycle.DisposableBean;
import org.springframework.beans.lifecycle.DisposableBeanAdapter;
import org.springframework.beans.lifecycle.InitializingBean;
import org.springframework.beans.processor.bean.BeanPostProcessor;
import org.springframework.beans.processor.bean.InstantiationAwareBeanPostProcessor;
import org.springframework.core.StringValueResolver;
import org.springframework.core.exception.BeansException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spring核心组件、IoC核心实现。
 * 同时实现 BeanDefinitionRegistry、BeanFactory、SingletonBeanRegistry 3个接口。
 * 如果把创建Bean类比为制作食品，那么这3个接口就是
 * - {@link BeanDefinitionRegistry} 加载并存储BeanDefinition（下游原材料供应商、原材料仓库）
 * - {@link BeanFactory}            根据BeanDefinition创建Bean（食品加工厂）
 * - {@link SingletonBeanRegistry}  存储单例Bean（食品仓库）
 *
 * 而DefaultListableBeanFactory就是那个打通上下游供应链的超级工厂。
 * 所以你会看到我在它内部定义了beanDefinitionMap、设计了bean创建流程/生命周期/扩展点、singletonObjects，
 * 这些都是DefaultListableBeanFactory整合以上3个接口的痕迹。
 */
public class DefaultListableBeanFactory implements BeanDefinitionRegistry, BeanFactory, SingletonBeanRegistry {
    /**
     * BeanDefinitionMap，根据BeanDefinition创建Bean
     */
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);
    /**
     * 单例池，缓存单例bean，避免重复创建
     */
    private final Map<String, Object> singletonObjects = new HashMap<>();
    /**
     * BeanPostProcessor扩展点：在Bean生命周期中做一些扩展操作
     */
    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();
    /**
     * 生命周期方法：Bean销毁时调用
     */
    private final Map<String, DisposableBean> disposableBeans = new HashMap<>();
    /**
     * 解析${}占位符
     */
    private final List<StringValueResolver> embeddedValueResolvers = new ArrayList<>();

    /************* 实现BeanDefinitionRegistry ************/

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        this.beanDefinitionMap.put(beanName, beanDefinition);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) throws BeansException {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null) {
            throw new BeansException("No bean named '" + beanName + "' is defined");
        }
        return beanDefinition;
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return beanDefinitionMap.containsKey(beanName);
    }

    @Override
    public String[] getBeanDefinitionNames() {
        Set<String> beanNames = beanDefinitionMap.keySet();
        return beanNames.toArray(new String[0]);
    }

    /************* 实现BeanFactory ************/

    @Override
    public Object getBean(String name) throws BeansException {
        Object sharedInstance = getSingleton(name);
        if (sharedInstance != null) {
            return sharedInstance;
        }

        BeanDefinition beanDefinition = getBeanDefinition(name);
        return createBean(name, beanDefinition);
    }

    protected Object createBean(String beanName, BeanDefinition beanDefinition) throws BeansException {
//        Object bean = resolveBeforeInstantiation(beanName, beanDefinition);
//        if (bean != null) {
//            return bean;
//        }

        return doCreateBean(beanName, beanDefinition);
    }

    protected Object doCreateBean(String beanName, BeanDefinition beanDefinition) {
        Object bean;
        try {
            // 实例化bean
            bean = createBeanInstance(beanDefinition);

            // 属性填充
            populateBean(beanName, beanDefinition, bean);

            // 初始化bean
            bean = initializeBean(beanName, bean, beanDefinition);
        } catch (Exception e) {
            throw new BeansException("Instantiation of bean '" + beanName + "' failed", e);
        }

        // 注册有销毁方法的bean
        registerDisposableBeanIfNecessary(beanName, bean, beanDefinition);

        // 单例bean放入缓存中，下次不再重复创建
        if (beanDefinition.isSingleton()) {
            addSingleton(beanName, bean);
        }
        return bean;
    }

    protected Object createBeanInstance(BeanDefinition beanDefinition) {
        Class<?> beanClass = beanDefinition.getBeanClass();
        try {
            Constructor<?> constructor = beanClass.getDeclaredConstructor();
            return constructor.newInstance();
        } catch (Exception e) {
            throw new BeansException("Failed to instantiate [" + beanClass.getName() + "]", e);
        }
    }

    private void populateBean(String beanName, BeanDefinition beanDefinition, Object bean) {
        // bean实例化后置处理：postProcessAfterInstantiation
        for (BeanPostProcessor beanPostProcessor : getBeanPostProcessors()) {
            if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
                if (!((InstantiationAwareBeanPostProcessor) beanPostProcessor).postProcessAfterInstantiation(bean, beanName)) {
                    return;
                }
            }
        }

        PropertyValues pvs = clonePvs(beanDefinition.getPropertyValues());

        // bean属性处理：在这里解析@Autowired和@Value，完成依赖注入和值注入
        for (BeanPostProcessor beanPostProcessor : getBeanPostProcessors()) {
            if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
                ((InstantiationAwareBeanPostProcessor) beanPostProcessor).postProcessProperties(pvs, bean, beanName);
            }
        }

        // 自动装配，XML时代遗留下来的的装配方式，现在极少用到（这里只演示ByName）
        int autowireMode = beanDefinition.getAutowireMode();
        if (autowireMode == BeanDefinition.AUTOWIRE_BY_NAME) {
            autowireByName(beanName, beanDefinition, bean, pvs);
        }

        // bean属性处理：把BeanDefinition里的值设置进去
        // 比如：<bean id="car" class="Car"><property name="brand" value="porsche"/></bean>
        applyPropertyValues(beanName, bean, beanDefinition, pvs);
    }

    private void autowireByName(String beanName, BeanDefinition beanDefinition, Object bean, PropertyValues pvs) {
        try {
            Map<String, String> property2DependentBean = new HashMap<>();
            for (PropertyValue propertyValue : pvs.getPropertyValues()) {
                if (propertyValue.getValue() instanceof BeanReference) {
                    BeanReference reference = (BeanReference) propertyValue.getValue();
                    if (beanDefinitionMap.containsKey(reference.getBeanName())) {
                        // <property name="bookRef" ref="book"/>
                        property2DependentBean.put(propertyValue.getName(), reference.getBeanName());
                    }
                }
            }
            for (Map.Entry<String, String> entry : property2DependentBean.entrySet()) {
                // 从容器获取依赖的bean
                Object dependentBean = getBean(entry.getValue());
                pvs.addPropertyValue(new PropertyValue(entry.getKey(), dependentBean));
            }
        } catch (Exception ex) {
            throw new BeansException("Error setting property values for bean: " + beanName, ex);
        }
    }

    protected void applyPropertyValues(String beanName, Object bean, BeanDefinition beanDefinition, PropertyValues pvs) {
        try {
            for (PropertyValue propertyValue : pvs.getPropertyValues()) {
                String name = propertyValue.getName();
                Object value = propertyValue.getValue();
                // 如果propertyValue到这里还没有被替换成实际的值，说明找不到，这里就不注入了
                boolean notReplacedYet = value instanceof BeanReference;
                if (!notReplacedYet) {
                    BeanUtils.setProperty(bean, name, value);
                }
            }
        } catch (Exception ex) {
            throw new BeansException("Error setting property values for bean: " + beanName, ex);
        }
    }

    protected Object initializeBean(String beanName, Object bean, BeanDefinition beanDefinition) {
        invokeAwareMethods(beanName, bean);

        // BeanPostProcessor扩展点：初始化前置处理
        Object wrappedBean = applyBeanPostProcessorsBeforeInitialization(bean, beanName);

        try {
            // 初始化方法
            invokeInitMethods(beanName, wrappedBean, beanDefinition);
        } catch (Throwable ex) {
            throw new BeansException("Invocation of init method of bean[" + beanName + "] failed", ex);
        }

        // BeanPostProcessor扩展点：初始化后后置处理
        wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
        return wrappedBean;
    }

    public Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName) throws BeansException {
        Object result = existingBean;
        for (BeanPostProcessor processor : getBeanPostProcessors()) {
            Object current = processor.postProcessBeforeInitialization(result, beanName);
            if (current == null) {
                return result;
            }
            result = current;
        }
        return result;
    }

    public Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName) throws BeansException {
        Object result = existingBean;
        for (BeanPostProcessor bp : getBeanPostProcessors()) {
            Object current = bp.postProcessAfterInitialization(result, beanName);
            if (current == null) {
                return result;
            }
            result = current;
        }
        return result;
    }

    private void invokeAwareMethods(String beanName, Object bean) {
        if (bean instanceof Aware) {
            if (bean instanceof BeanFactoryAware) {
                ((BeanFactoryAware) bean).setBeanFactory(this);
            }
        }
    }

    /**
     * 执行bean的初始化方法
     */
    protected void invokeInitMethods(String beanName, Object bean, BeanDefinition beanDefinition) throws Throwable {
        if (bean instanceof InitializingBean) {
            ((InitializingBean) bean).afterPropertiesSet();
        }
        String initMethodName = beanDefinition.getInitMethodName();
        // 避免执行两次相同的初始化方法（假设init-method也叫afterPropertiesSet，那么afterPropertiesSet会执行两次）
        if (StrUtil.isNotEmpty(initMethodName) && !(bean instanceof InitializingBean && "afterPropertiesSet".equals(initMethodName))) {
            Method initMethod = ClassUtil.getPublicMethod(beanDefinition.getBeanClass(), initMethodName);
            if (initMethod == null) {
                throw new BeansException("Could not find an init method named '" + initMethodName + "' on bean with name '" + beanName + "'");
            }
            // 反射
            initMethod.invoke(bean);
        }
    }

    /**
     * 注册有销毁方法的bean，即bean继承自DisposableBean或有自定义的销毁方法
     */
    protected void registerDisposableBeanIfNecessary(String beanName, Object bean, BeanDefinition beanDefinition) {
        // 只有singleton类型bean会执行销毁方法
        if (beanDefinition.isSingleton()) {
            if (bean instanceof DisposableBean || StrUtil.isNotEmpty(beanDefinition.getDestroyMethodName())) {
                disposableBeans.put(beanName, new DisposableBeanAdapter(bean, beanName, beanDefinition));
            }
        }
    }

    public void addEmbeddedValueResolver(StringValueResolver valueResolver) {
        this.embeddedValueResolvers.add(valueResolver);
    }

    public String resolveEmbeddedValue(String value) {
        String result = value;
        for (StringValueResolver resolver : this.embeddedValueResolvers) {
            result = resolver.resolveStringValue(result);
        }
        return result;
    }

    @Override
    public <T> T getBean(Class<T> requiredType) throws BeansException {
        List<String> beanNames = new ArrayList<>();
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            Class<?> beanClass = entry.getValue().getBeanClass();
            if (requiredType.isAssignableFrom(beanClass)) {
                beanNames.add(entry.getKey());
            }
        }
        if (beanNames.size() == 1) {
            return getBean(beanNames.get(0), requiredType);
        }
        throw new BeansException(requiredType + "expected single bean but found " +
                beanNames.size() + ": " + beanNames);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return ((T) getBean(name));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
        Map<String, T> result = new HashMap<>();
        beanDefinitionMap.forEach((beanName, beanDefinition) -> {
            Class<?> beanClass = beanDefinition.getBeanClass();
            if (type.isAssignableFrom(beanClass)) {
                T bean = (T) getBean(beanName);
                result.put(beanName, bean);
            }
        });
        return result;
    }

    public void preInstantiateSingletons() throws BeansException {
        beanDefinitionMap.forEach((beanName, beanDefinition) -> {
            // 单例 && 非延迟加载的bean，提前实例化
            if (beanDefinition.isSingleton() && !beanDefinition.isLazyInit()) {
                getBean(beanName);
            }
        });
    }

    public void destroySingletons() {
        ArrayList<String> beanNames = new ArrayList<>(disposableBeans.keySet());
        for (String beanName : beanNames) {
            DisposableBean disposableBean = disposableBeans.remove(beanName);
            try {
                disposableBean.destroy();
            } catch (Exception e) {
                throw new BeansException("Destroy method on bean with name '" + beanName + "' threw an exception", e);
            }
        }

        clearSingletonCache();
    }

    private void clearSingletonCache() {
        this.singletonObjects.clear();
    }

    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        // 有则覆盖
        this.beanPostProcessors.remove(beanPostProcessor);
        this.beanPostProcessors.add(beanPostProcessor);
    }

    public List<BeanPostProcessor> getBeanPostProcessors() {
        return this.beanPostProcessors;
    }

    private PropertyValues clonePvs(PropertyValues propertyValues) {
        PropertyValues pvs = new PropertyValues();
        for (PropertyValue propertyValue : propertyValues.getPropertyValues()) {
            pvs.addPropertyValue(propertyValue);
        }
        return pvs;
    }

    /************* 实现SingletonBeanRegistry ************/

    @Override
    public Object getSingleton(String beanName) {
        return singletonObjects.get(beanName);
    }

    @Override
    public void addSingleton(String beanName, Object singletonObject) {
        singletonObjects.put(beanName, singletonObject);
    }

    @Override
    public boolean containsSingleton(String beanName) {
        return singletonObjects.containsKey(beanName);
    }
}
