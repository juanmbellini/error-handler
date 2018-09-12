package com.bellotapps.utils.error_handler;

/**
 * Defines behaviour for an object that knows how to handle any kind of {@link Throwable}.
 * Handling an exception means getting data for the response that must be returned to the API consumer.
 *
 * @author Juan Marcos Bellini
 * @see ExceptionHandler
 * @see ExceptionHandlerObject
 * @see EnableErrorHandler
 */
@FunctionalInterface
public interface ErrorHandler {

    /**
     * Handles the given {@code exception},
     * returning as a result an object with the data to be returned to the API consumer.
     *
     * @param exception The {@link Throwable} to be handled.
     * @param <T>       The concrete subclass type of {@link Throwable}.
     * @param <E>       Concrete type of entity to be sent in the response.
     * @return a {@link HandlingResult} with the data to be returned to the API consumer.
     */
    <T extends Throwable, E> HandlingResult<E> handle(T exception);
}
