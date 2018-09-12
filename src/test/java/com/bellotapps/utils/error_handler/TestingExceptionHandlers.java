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
