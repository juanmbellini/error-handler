package com.bellotapps.utils.error_handler;

/**
 * Class containing some {@link ExceptionHandler} implementations.
 */
/* package */ class TestingExceptionHandlers {

    /* package */ static class NullPointerExceptionHandler
            implements ExceptionHandler<NullPointerException, String> {

        @Override
        public ErrorHandler.HandlingResult<String> handle(NullPointerException exception) {
            return new ErrorHandler.HandlingResult<>(400, "Was null");
        }
    }

    /* package */ static class IllegalArgumentExceptionHandler
            implements ExceptionHandler<IllegalArgumentException, String> {

        @Override
        public ErrorHandler.HandlingResult<String> handle(IllegalArgumentException exception) {
            return new ErrorHandler.HandlingResult<>(400, "illegal argument");
        }
    }

    /* package */ static class RuntimeExceptionHandler
            implements ExceptionHandler<RuntimeException, String> {

        @Override
        public ErrorHandler.HandlingResult<String> handle(RuntimeException exception) {
            return new ErrorHandler.HandlingResult<>(500, "runtime exception");
        }
    }

    /* package */ static class ThrowableHandler
            implements ExceptionHandler<Throwable, String> {

        @Override
        public ErrorHandler.HandlingResult<String> handle(Throwable exception) {
            return new ErrorHandler.HandlingResult<>(500, "a throwable was not caught");
        }
    }
}
