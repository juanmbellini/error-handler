package com.bellotapps.utils.error_handler;

import java.lang.annotation.*;

/**
 * Indicates that an object is an
 * {@link com.bellotapps.utils.error_handler.ExceptionHandler}.
 * <p>
 * Note that just using the annotation is not enough for being registered in the
 * {@link com.bellotapps.utils.error_handler.ErrorHandler}.
 * The annotated class must also implement the interface.
 *
 * @author Juan Marcos Bellini
 * @see ExceptionHandler
 * @see ErrorHandler
 * @see EnableErrorHandler
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExceptionHandlerObject {
}
