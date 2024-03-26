package org.springframework.core.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * 资源的抽象
 * 资源可以是一个XML、File、URL，但不论如何，都需要{@link ResourceLoader}加载到内存才能操作
 */
public interface Resource {

    /**
     * 将Resource变成InputStream
     */
    InputStream getInputStream() throws IOException;

}
