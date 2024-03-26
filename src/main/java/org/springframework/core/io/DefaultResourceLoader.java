package org.springframework.core.io;

public class DefaultResourceLoader implements ResourceLoader {

	public static final String CLASSPATH_URL_PREFIX = "classpath:";

	@Override
	public Resource getResource(String location) {
		if (location.startsWith(CLASSPATH_URL_PREFIX)) {
			// classpath下的资源
			return new ClassPathResource(location.substring(CLASSPATH_URL_PREFIX.length()));
		} else {
			throw new RuntimeException("only supports xml resource");
		}
	}
}
