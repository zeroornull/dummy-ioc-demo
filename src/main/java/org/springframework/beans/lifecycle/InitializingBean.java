package org.springframework.beans.lifecycle;

/**
 * Bean生命周期钩子：Bean初始化时调用
 * 如果实现该接口，则在Bean属性填充完毕后（@Autowired、@Value这些都执行了），执行afterPropertiesSet方法进行初始化
 */
public interface InitializingBean {

    void afterPropertiesSet() throws Exception;
}
