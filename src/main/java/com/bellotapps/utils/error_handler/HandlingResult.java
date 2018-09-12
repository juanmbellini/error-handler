package com.bellotapps.utils.error_handler;

import org.springframework.util.Assert;

/**
 * Container class holding the results of handling a {@link Throwable}.
 *
 * @param <E> Concrete type of entity to be sent in the response.
 */
public class HandlingResult<E> {

    /**
     * The HTTP status code that must be returned in the response.
     */
    private final int httpErrorCode;

    /**
     * The entity that will be returned in the response. Can be null.
     */
    private final E errorRepresentationEntity;


    /**
     * Constructor.
     *
     * @param httpErrorCode             The HTTP status code that must be returned in the response.
     * @param errorRepresentationEntity The entity that will be returned in the response. Can be null.
     */
    private HandlingResult(final int httpErrorCode, final E errorRepresentationEntity) {
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
    public E getErrorRepresentationEntity() {
        return errorRepresentationEntity;
    }

    public static <T> HandlingResult<T> withPayload(final int httpErrorCode, final T errorRepresentationEntity) {
        Assert.notNull(errorRepresentationEntity,
                "When using the withPayload factory method, a payload must be set." +
                        " For null payload results use the justErrorCode factory method");
        return new HandlingResult<>(httpErrorCode, errorRepresentationEntity);
    }

    public static <T> HandlingResult<T> justErrorCode(final int httpErrorCode) {
        return new HandlingResult<>(httpErrorCode, null);
    }
}
