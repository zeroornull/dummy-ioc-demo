package org.springframework.core.io;

/**
 * 资源加载器：给定一个location，把location指向的资源加载到内存中
 */
public interface ResourceLoader {

    Resource getResource(String location);
}
