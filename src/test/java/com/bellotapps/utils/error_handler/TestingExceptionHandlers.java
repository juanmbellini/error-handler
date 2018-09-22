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
 * Class containing some {@link ExceptionHandler} implementations.
 */
public class TestingExceptionHandlers {

    /**
     * An {@link ExceptionHandler} for {@link NullPointerException}.
     */
    public static class NullPointerExceptionHandler implements ExceptionHandler<NullPointerException, String> {
        @Override
        public HandlingResult<String> handle(NullPointerException exception) {
            return HandlingResult.withPayload(400, "Was null");
        }
    }

    /**
     * An {@link ExceptionHandler} for {@link IllegalArgumentException}.
     */
    public static class IllegalArgumentExceptionHandler implements ExceptionHandler<IllegalArgumentException, String> {
        @Override
        public HandlingResult<String> handle(IllegalArgumentException exception) {
            return HandlingResult.withPayload(400, "illegal argument");
        }
    }

    /**
     * An {@link ExceptionHandler} for {@link RuntimeException}.
     */
    public static class RuntimeExceptionHandler implements ExceptionHandler<RuntimeException, String> {
        @Override
        public HandlingResult<String> handle(RuntimeException exception) {
            return HandlingResult.withPayload(500, "runtime exception");
        }
    }

    /**
     * An {@link ExceptionHandler} for {@link Throwable}.
     */
    public static class ThrowableHandler implements ExceptionHandler<Throwable, String> {
        @Override
        public HandlingResult<String> handle(Throwable exception) {
            return HandlingResult.withPayload(500, "a throwable was not caught");
        }
    }
}
