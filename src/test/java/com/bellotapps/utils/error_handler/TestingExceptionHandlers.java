package com.bellotapps.utils.error_handler;

/**
 * Class containing some {@link ExceptionHandler} implementations.
 */
/* package */ class TestingExceptionHandlers {

    /* package */ static class NullPointerExceptionHandler implements ExceptionHandler<NullPointerException> {

        @Override
        public ErrorHandler.HandlingResult handle(NullPointerException exception) {
            return new ErrorHandler.HandlingResult(400, "Was null");
        }
    }

    /* package */ static class IllegalArgumentExceptionHandler implements ExceptionHandler<IllegalArgumentException> {

        @Override
        public ErrorHandler.HandlingResult handle(IllegalArgumentException exception) {
            return new ErrorHandler.HandlingResult(400, "illegal argument");
        }
    }

    /* package */ static class RuntimeExceptionHandler implements ExceptionHandler<RuntimeException> {

        @Override
        public ErrorHandler.HandlingResult handle(RuntimeException exception) {
            return new ErrorHandler.HandlingResult(500, "runtime exception");
        }
    }

    /* package */ static class ThrowableHandler implements ExceptionHandler<Throwable> {

        @Override
        public ErrorHandler.HandlingResult handle(Throwable exception) {
            return new ErrorHandler.HandlingResult(500, "a throwable was not caught");
        }
    }
}
