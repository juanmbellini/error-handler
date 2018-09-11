package com.bellotapps.utils.error_handler;

/**
 * Defines behaviour for an object that can handles a given type of {@link Throwable}.
 * In order to register a concrete implementation of this interface in the {@link ErrorHandler},
 * the implementing class must be annotated with
 * {@link ExceptionHandlerObject}.
 *
 * @param <T> The concrete type of {@link Throwable} that will be handled by the object implementing this interface.
 * @param <E> The concrete type of entity being sent in the handling result.
 * @author Juan Marcos Bellini
 * @see ExceptionHandlerObject
 * @see ErrorHandler
 * @see EnableErrorHandler
 */
@FunctionalInterface
public interface ExceptionHandler<T extends Throwable, E> {

    /**
     * Handles the given {@code exception}.
     *
     * @param exception The exception to be handled.
     * @return The {@link ErrorHandler.HandlingResult} of handling the given {@code exception}.
     */
    ErrorHandler.HandlingResult<E> handle(T exception);
}