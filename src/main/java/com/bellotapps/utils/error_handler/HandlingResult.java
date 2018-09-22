/*
 * Copyright 2018 BellotApps
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
