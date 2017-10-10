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
public interface ErrorHandler {

    /**
     * Handles the given {@code exception},
     * returning as a result an object with the data to be returned to the API consumer.
     *
     * @param exception The {@link Throwable} to be handled.
     * @param <T>       The concrete subclass type of {@link Throwable}.
     * @return a {@link HandlingResult} with the data to be returned to the API consumer.
     */
    <T extends Throwable> HandlingResult handle(T exception);


    /**
     * Container class holding the results of handling a {@link Throwable}.
     */
    final class HandlingResult {

        /**
         * The HTTP status code that must be returned in the response.
         */
        private final int httpErrorCode;

        /**
         * The entity that will be returned in the response. Can be null.
         */
        private final Object errorRepresentationEntity;


        /**
         * Constructor.
         *
         * @param httpErrorCode             The HTTP status code that must be returned in the response.
         * @param errorRepresentationEntity The entity that will be returned in the response. Can be null.
         */
        public HandlingResult(int httpErrorCode, Object errorRepresentationEntity) {
            this.httpErrorCode = httpErrorCode;
            this.errorRepresentationEntity = errorRepresentationEntity;
        }

        /**
         * @return The HTTP status code that must be returned in the response.
         */
        public int getHttpErrorCode() {
            return httpErrorCode;
        }

        /**
         * @return The entity that will be returned in the response. Can be null.
         */
        public Object getErrorRepresentationEntity() {
            return errorRepresentationEntity;
        }
    }
}
