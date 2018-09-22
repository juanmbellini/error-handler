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
    <T extends Throwable, E> HandlingResult<E> handle(final T exception);
}
