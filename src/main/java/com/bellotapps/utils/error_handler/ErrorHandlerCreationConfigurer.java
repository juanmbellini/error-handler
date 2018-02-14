package com.bellotapps.utils.error_handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Configuration class extended from {@link AnnotationErrorHandlerCreationConfigurer},
 * using {@link EnableErrorHandler} annotation.
 */
@Configuration
@EnableErrorHandlerFactory
/* package */ class ErrorHandlerCreationConfigurer
        extends AnnotationErrorHandlerCreationConfigurer<EnableErrorHandler> {

    /**
     * Protected constructor, in order to let it be initialized only by subclasses.
     *
     * @param errorHandlerFactory The {@link ErrorHandlerFactory} to be used to create the {@link ErrorHandler} bean.
     */
    @Autowired
    public ErrorHandlerCreationConfigurer(ErrorHandlerFactory errorHandlerFactory) {
        super(errorHandlerFactory);
    }

    @Override
    protected Class<EnableErrorHandler> getAnnotationClass() {
        return EnableErrorHandler.class;
    }

    @Override
    protected Collection<String> getPackagesCollectionFromAnnotation(EnableErrorHandler enableErrorHandler) {
        final String[] basePackages = enableErrorHandler.basePackages();
        final String[] basePackageClasses = Arrays.stream(enableErrorHandler.basePackageClasses())
                .map(Class::getPackage)
                .map(Package::getName)
                .toArray(String[]::new);
        return Stream.concat(Arrays.stream(basePackages), Arrays.stream(basePackageClasses))
                .collect(Collectors.toSet());
    }
}
