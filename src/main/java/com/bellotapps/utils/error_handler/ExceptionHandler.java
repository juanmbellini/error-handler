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
     * @return The {@link HandlingResult} of handling the given {@code exception}.
     */
    HandlingResult<E> handle(final T exception);
}
