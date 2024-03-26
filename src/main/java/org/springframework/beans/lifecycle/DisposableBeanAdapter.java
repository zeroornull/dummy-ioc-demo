package org.springframework.beans.lifecycle;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.beandefinition.definition.BeanDefinition;
import org.springframework.core.exception.BeansException;

import java.lang.reflect.Method;

/**
 * 适配器模式，无论是实现DisposableBean接口还是使用destroy-method属性，都包装成DisposableBean
 */
public class DisposableBeanAdapter implements DisposableBean {

	private final Object bean;

	private final String beanName;

	private final String destroyMethodName;

	public DisposableBeanAdapter(Object bean, String beanName, BeanDefinition beanDefinition) {
		this.bean = bean;
		this.beanName = beanName;
		this.destroyMethodName = beanDefinition.getDestroyMethodName();
	}

	@Override
	public void destroy() throws Exception {
		if (bean instanceof DisposableBean) {
			((DisposableBean) bean).destroy();
		}

		// 避免执行两次相同的销毁方法（destroy-method也叫destroy，那么destroy会执行两次）
		if (StrUtil.isNotEmpty(destroyMethodName) && !(bean instanceof DisposableBean && "destroy".equals(this.destroyMethodName))) {
			Method destroyMethod = ClassUtil.getPublicMethod(bean.getClass(), destroyMethodName);
			if (destroyMethod == null) {
				throw new BeansException("Couldn't find a destroy method named '" + destroyMethodName + "' on bean with name '" + beanName + "'");
			}
			// 反射
			destroyMethod.invoke(bean);
		}
	}
}
