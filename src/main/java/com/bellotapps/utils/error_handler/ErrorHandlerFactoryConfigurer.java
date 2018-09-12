package com.bellotapps.utils.error_handler;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for {@link ErrorHandlerFactory}.
 */
@Configuration
public class ErrorHandlerFactoryConfigurer implements InitializingBean, BeanFactoryAware, BeanClassLoaderAware {

    /**
     * The {@link BeanFactory} to be used by the generated {@link ErrorHandlerFactory}.
     */
    private BeanFactory beanFactory;

    /**
     * The {@link ClassLoader} to be used by the generated {@link ErrorHandlerFactory}.
     */
    private ClassLoader classLoader;

    /**
     * Indicates whether this configuration object is initialized.
     */
    private boolean initialized;

    /**
     * Constructor.
     */
    public ErrorHandlerFactoryConfigurer() {
        this.initialized = false;
    }

    @Bean
    public ErrorHandlerFactory errorHandlerFactory() {
        if (!initialized || classLoader == null || beanFactory == null) {
            throw new BeanInitializationException("The ErrorHandlerFactoryConfigurer was not correctly initialized");
        }
        return new ErrorHandlerFactory(classLoader, beanFactory);
    }

    @Override
    public void setBeanClassLoader(final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setBeanFactory(final BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterPropertiesSet() {
        this.initialized = true;
    }
}
