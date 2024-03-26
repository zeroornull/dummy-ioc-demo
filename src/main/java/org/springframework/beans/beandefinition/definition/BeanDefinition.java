package org.springframework.beans.beandefinition.definition;

import java.util.Objects;

/**
 * Spring的核心概念之一，定义了如何创建一个Bean，包括Bean的类名、属性值、是否单例、生命周期方法等。
 *
 * <p>
 * 为什么需要额外定义一个BeanDefinition，难道不是多此一举？非也。Spring作为IoC容器，可以帮我们管理对象的创建、赋值（注入）、销毁等。
 * 这些被Spring托管的对象被称为Bean。引入Spring后，对Bean的主导权由程序员转移到了框架，即所谓的控制反转。
 * 但Spring也不是这么无情，虽然Bean的创建权被它剥夺了，但Spring对外暴露了BeanDefinition抽象，我们可以通过BeanDefinition指导Spring如何创建一个Bean。
 * 相当于塞给Spring一张说明书：哥，你看这道菜能帮我做一下吗？（紧张地搓手手）
 *
 * <p>
 * 不论是Bean标签、@Component还是@Bean，本质都是为了定义一个BeanDefinition，然后最终由Spring根据BeanDefinition创建Bean
 */
public class BeanDefinition {
    /******* 常量 *******/
    public static String SCOPE_SINGLETON = "singleton";
    public static String SCOPE_PROTOTYPE = "prototype";
    public static final int AUTOWIRE_NO = 0;
    public static final int AUTOWIRE_BY_NAME = 1;
    public static final int AUTOWIRE_BY_TYPE = 2;

    /******* attribute：比如指定beanClass、initMethodName、scope等 *******/
    private Class<?> beanClass;
    private boolean singleton = true;
    private boolean prototype = false;
    private boolean lazyInit = false;
    private String initMethodName;
    private String destroyMethodName;
    private int autowireMode = AUTOWIRE_NO;

    /******* properties：属性值，比如Person.name=bravo *******/
    private PropertyValues propertyValues;

    public BeanDefinition(Class<?> beanClass) {
        this(beanClass, null);
    }

    public BeanDefinition(Class<?> beanClass, PropertyValues propertyValues) {
        this.beanClass = beanClass;
        this.propertyValues = propertyValues != null ? propertyValues : new PropertyValues();
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    public void setScope(String scope) {
        this.singleton = SCOPE_SINGLETON.equals(scope);
        this.prototype = SCOPE_PROTOTYPE.equals(scope);
    }

    public boolean isSingleton() {
        return this.singleton;
    }

    public boolean isPrototype() {
        return this.prototype;
    }

    public void setLazyInit(boolean b) {
        lazyInit = b;
    }

    public boolean isLazyInit() {
        return lazyInit;
    }

    public String getInitMethodName() {
        return initMethodName;
    }

    public void setInitMethodName(String initMethodName) {
        this.initMethodName = initMethodName;
    }

    public String getDestroyMethodName() {
        return destroyMethodName;
    }

    public void setDestroyMethodName(String destroyMethodName) {
        this.destroyMethodName = destroyMethodName;
    }

    public int getAutowireMode() {
        return autowireMode;
    }

    public void setAutowireMode(int autowireMode) {
        this.autowireMode = autowireMode;
    }

    public PropertyValues getPropertyValues() {
        return propertyValues;
    }

    public void setPropertyValues(PropertyValues propertyValues) {
        this.propertyValues = propertyValues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BeanDefinition that = (BeanDefinition) o;
        return beanClass.equals(that.beanClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(beanClass);
    }
}
