package com.bellotapps.utils.error_handler;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Optional;

/**
 * Abstract configuration class for {@link ErrorHandler} creation,
 * using an {@link Annotation} to get packages to be scanned.
 */
public abstract class AnnotationErrorHandlerCreationConfigurer<A extends Annotation>
        extends AbstractErrorHandlerCreationConfigurer {

    /**
     * Protected constructor, in order to let it be initialized only by subclasses.
     *
     * @param errorHandlerFactory The {@link ErrorHandlerFactory} to be used to create the {@link ErrorHandler} bean.
     */
    protected AnnotationErrorHandlerCreationConfigurer(final ErrorHandlerFactory errorHandlerFactory) {
        super(errorHandlerFactory);
    }

    @Override
    protected Collection<String> getPackagesCollection() throws ClassNotFoundException {
        return getPackagesCollectionFromAnnotation(retrieveAnnotation());
    }

    /**
     * Finds the {@link EnableErrorHandler} that was used to import this {@link Configuration} class.
     *
     * @return The {@link EnableErrorHandler} used to import this {@link Configuration} class.
     * @throws ClassNotFoundException   In case the importing class is not found.
     * @throws IllegalArgumentException In case the importing class was not annotated with {@link EnableErrorHandler}.
     * @see EnableErrorHandler
     */
    private A retrieveAnnotation() throws ClassNotFoundException {
        final Class<?> importingClass = Class.forName(getImportMetadata().getClassName());
        return Optional.ofNullable(AnnotationUtils.findAnnotation(importingClass, getAnnotationClass()))
                .orElseThrow(IllegalArgumentException::new);
    }

    /**
     * Retrieves a {@link Collection} of packages names from the given {@code annotation}.
     *
     * @param annotation The annotation from which packages will be retrieved.
     * @return A {@link Collection} of packages names to be retrieved by the {@link #getPackagesCollection()} method.
     */
    protected abstract Collection<String> getPackagesCollectionFromAnnotation(final A annotation);

    /**
     * @return The {@link Class} of the annotation used to retrieve packages from.
     */
    protected abstract Class<A> getAnnotationClass();
}
