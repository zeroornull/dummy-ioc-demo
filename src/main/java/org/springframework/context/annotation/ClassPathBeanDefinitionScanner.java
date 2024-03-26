package org.springframework.context.annotation;

import cn.hutool.core.util.StrUtil;
import org.springframework.beans.beandefinition.definition.BeanDefinition;
import org.springframework.beans.beandefinition.registry.BeanDefinitionRegistry;
import org.springframework.beans.processor.bean.AutowiredAnnotationBeanPostProcessor;

import java.util.Set;

/**
 * BeanDefinition扫描器
 */
public class ClassPathBeanDefinitionScanner extends ClassPathScanningCandidateComponentProvider {

	public static final String AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME = "org.springframework.context.annotation.internalAutowiredAnnotationProcessor";

	private final BeanDefinitionRegistry registry;

	public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
		this.registry = registry;
	}

	public void doScan(String... basePackages) {
		for (String basePackage : basePackages) {
			// 此时BeanDefinition只包含Class信息，for循环里再进一步完善
			Set<BeanDefinition> candidates = findCandidateComponents(basePackage);
			for (BeanDefinition candidate : candidates) {
				// 解析bean的作用域
				String beanScope = resolveBeanScope(candidate);
				if (StrUtil.isNotEmpty(beanScope)) {
					candidate.setScope(beanScope);
				}
				// 生成bean的名称
				String beanName = determineBeanName(candidate);
				// 注册BeanDefinition
				registry.registerBeanDefinition(beanName, candidate);
			}
		}

		// 注册处理@Autowired和@Value注解的BeanPostProcessor
		registry.registerBeanDefinition(AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME, new BeanDefinition(AutowiredAnnotationBeanPostProcessor.class));
	}

	/**
	 * 获取bean的作用域
	 */
	private String resolveBeanScope(BeanDefinition beanDefinition) {
		Class<?> beanClass = beanDefinition.getBeanClass();
		Scope scope = beanClass.getAnnotation(Scope.class);
		if (scope != null) {
			return scope.value();
		}

		return StrUtil.EMPTY;
	}


	/**
	 * 生成bean的名称
	 */
	private String determineBeanName(BeanDefinition beanDefinition) {
		Class<?> beanClass = beanDefinition.getBeanClass();
		Component component = beanClass.getAnnotation(Component.class);
		String value = component.value();
		if (StrUtil.isEmpty(value)) {
			value = StrUtil.lowerFirst(beanClass.getSimpleName());
		}
		return value;
	}
}
