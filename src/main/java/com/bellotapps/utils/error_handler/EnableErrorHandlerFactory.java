package com.bellotapps.utils.error_handler;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enables the {@link ErrorHandlerFactory} configuration.
 * Use it to automatically have a bean of {@link ErrorHandlerFactory},
 * defined according to {@link ErrorHandlerFactoryConfigurer}.
 *
 * @see ErrorHandlerFactory
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({ErrorHandlerFactoryConfigurer.class,})
public @interface EnableErrorHandlerFactory {
}
