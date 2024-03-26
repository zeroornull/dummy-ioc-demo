package org.springframework.test.beans.beandefinition.loader;


import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * ResourceLoader负责将各类资源加载到内存中，是一个很有用的组件，可以单独使用。
 * Spring把URL、XML、File这些常见资源抽象成{@link Resource}，在dummy-ioc中我们只实现了{@link ClassPathResource}
 * 简而言之，{@link ResourceLoader}是用来加载资源（比如XML）的，加载后的资源在内存中用{@link Resource}表示。
 */
public class ResourceLoaderTest {

    @Test
    public void testReadResource() throws IOException {
        // 加载XML资源
        DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource("classpath:1_bean-definition-reader-xml.xml");
        Assert.assertNotNull(resource.getInputStream());
        printInputStream(resource.getInputStream());
    }

    private void printInputStream(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
