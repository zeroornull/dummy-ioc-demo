package org.springframework.context.event.multicaster;

import org.springframework.core.common.Nullable;
import org.springframework.beans.BeanFactory;
import org.springframework.beans.DefaultListableBeanFactory;
import org.springframework.beans.aware.BeanFactoryAware;
import org.springframework.context.event.event.ApplicationEvent;
import org.springframework.context.event.listener.ApplicationListener;
import org.springframework.core.exception.BeansException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractApplicationEventMulticaster implements ApplicationEventMulticaster, BeanFactoryAware {

    public final Set<ApplicationListener<?>> applicationListeners = new LinkedHashSet<>();

    // 目前没啥用，这里只是为了和Spring保持接口一致
    @Nullable
    private DefaultListableBeanFactory beanFactory;

    @Override
    public void addApplicationListener(ApplicationListener<?> listener) {
        applicationListeners.add(listener);
    }

    @Override
    public void removeApplicationListener(ApplicationListener<?> listener) {
        applicationListeners.remove(listener);
    }

    protected Collection<ApplicationListener<?>> getApplicationListeners(ApplicationEvent event) {
        if (applicationListeners.isEmpty()) {
            return Collections.emptySet();
        }
        return applicationListeners.stream()
                .filter(listener -> supportsEvent(listener, event))
                .collect(Collectors.toSet());
    }

    /**
     * 监听器是否对该事件感兴趣
     */
    protected boolean supportsEvent(ApplicationListener<?> applicationListener, ApplicationEvent event) {
        Type type = applicationListener.getClass().getGenericInterfaces()[0];
        Type actualTypeArgument = ((ParameterizedType) type).getActualTypeArguments()[0];
        String className = actualTypeArgument.getTypeName();
        Class<?> eventClassName;
        try {
            eventClassName = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new BeansException("wrong event class name: " + className);
        }
        return eventClassName.isAssignableFrom(event.getClass());
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        if (!(beanFactory instanceof DefaultListableBeanFactory)) {
            throw new IllegalStateException("Not running in a ConfigurableBeanFactory: " + beanFactory);
        }
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }
}
