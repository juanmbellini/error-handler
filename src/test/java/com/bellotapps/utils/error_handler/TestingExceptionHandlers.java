package com.bellotapps.utils.error_handler;

/**
 * Class containing some {@link ExceptionHandler} implementations.
 */
/* package */ class TestingExceptionHandlers {

    /* package */ static class NullPointerExceptionHandler
            implements ExceptionHandler<NullPointerException, String> {

        @Override
        public HandlingResult<String> handle(NullPointerException exception) {
            return HandlingResult.withPayload(400, "Was null");
        }
    }

    /* package */ static class IllegalArgumentExceptionHandler
            implements ExceptionHandler<IllegalArgumentException, String> {

        @Override
        public HandlingResult<String> handle(IllegalArgumentException exception) {
            return HandlingResult.withPayload(400, "illegal argument");
        }
    }

    /* package */ static class RuntimeExceptionHandler
            implements ExceptionHandler<RuntimeException, String> {

        @Override
        public HandlingResult<String> handle(RuntimeException exception) {
            return HandlingResult.withPayload(500, "runtime exception");
        }
    }

    /* package */ static class ThrowableHandler
            implements ExceptionHandler<Throwable, String> {

        @Override
        public HandlingResult<String> handle(Throwable exception) {
            return HandlingResult.withPayload(500, "a throwable was not caught");
        }
    }
}
