package org.springframework.beans.beandefinition.definition;

/**
 * 一个bean对另一个bean的引用
 */
public class BeanReference {

    private final String beanName;


    public BeanReference(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanName() {
        return beanName;
    }
}
