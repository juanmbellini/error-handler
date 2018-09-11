package com.bellotapps.utils.error_handler;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Testing class for {@link ErrorHandlerImpl}.
 */
public class ErrorHandlerImplTest {


    private static final String NOT_THE_DEFAULT_HANDLER_ERROR_MESSAGE =
            "The error handler was not initialized with the declared default handler";

    private static final String NOT_HANDLED_AS_EXPECTED = "The error handler did not handle an exception as expected";


    @Test
    public void testListOfHandlersWithDefault() {
        final ExceptionHandler<NullPointerException, String> nullPointerExceptionHandler =
                new TestingExceptionHandlers.NullPointerExceptionHandler();
        final ExceptionHandler<IllegalArgumentException, String> illegalArgumentHandler =
                new TestingExceptionHandlers.IllegalArgumentExceptionHandler();
        final ExceptionHandler<RuntimeException, String> runtimeExceptionHandler =
                new TestingExceptionHandlers.RuntimeExceptionHandler();
        final ExceptionHandler<Throwable, String> throwableHandler =
                new TestingExceptionHandlers.ThrowableHandler();

        final List<ExceptionHandler<? extends Throwable, ?>> handlers = Stream
                .of(nullPointerExceptionHandler, illegalArgumentHandler, runtimeExceptionHandler, throwableHandler)
                .collect(Collectors.toList());

        final ErrorHandlerImpl errorHandler = new ErrorHandlerImpl(handlers);

        // Test null pointer exception
        testHandle(new NullPointerException(), errorHandler, nullPointerExceptionHandler, NOT_HANDLED_AS_EXPECTED);
        // Test illegal argument
        testHandle(new IllegalArgumentException(), errorHandler, illegalArgumentHandler, NOT_HANDLED_AS_EXPECTED);
        // Test runtime exception
        testHandle(new RuntimeException(), errorHandler, runtimeExceptionHandler, NOT_HANDLED_AS_EXPECTED);
        // Test a runtime exception that does not have handler
        testHandle(new NoSuchElementException(), errorHandler, runtimeExceptionHandler, NOT_HANDLED_AS_EXPECTED);
        // Test a throwable is handled
        testHandle(new Throwable(), errorHandler, throwableHandler, NOT_HANDLED_AS_EXPECTED);
    }

    @Test
    public void testListOfHandlersWithoutDefault() throws NoSuchFieldException, IllegalAccessException {
        final ExceptionHandler<NullPointerException, String> nullPointerExceptionHandler =
                new TestingExceptionHandlers.NullPointerExceptionHandler();
        final ExceptionHandler<IllegalArgumentException, String> illegalArgumentHandler =
                new TestingExceptionHandlers.IllegalArgumentExceptionHandler();
        final ExceptionHandler<RuntimeException, String> runtimeExceptionHandler =
                new TestingExceptionHandlers.RuntimeExceptionHandler();

        final List<ExceptionHandler<? extends Throwable, ?>> handlers =
                Stream.of(nullPointerExceptionHandler, illegalArgumentHandler, runtimeExceptionHandler)
                        .collect(Collectors.toList());

        final ErrorHandlerImpl errorHandler = new ErrorHandlerImpl(handlers);

        // Test null pointer exception
        testHandle(new NullPointerException(), errorHandler, nullPointerExceptionHandler, NOT_HANDLED_AS_EXPECTED);
        // Test illegal argument
        testHandle(new IllegalArgumentException(), errorHandler, illegalArgumentHandler, NOT_HANDLED_AS_EXPECTED);
        // Test runtime exception
        testHandle(new RuntimeException(), errorHandler, runtimeExceptionHandler, NOT_HANDLED_AS_EXPECTED);
        // Test a runtime exception that does not have handler
        testHandle(new NoSuchElementException(), errorHandler, runtimeExceptionHandler, NOT_HANDLED_AS_EXPECTED);
        // Test a throwable is handled
        testHandle(new Throwable(), errorHandler, getDefaultHandler(errorHandler), NOT_HANDLED_AS_EXPECTED);
    }

    @Test
    public void testDefaultHandlerIsAddedIfNoHandlerForThrowableIsDefined()
            throws NoSuchFieldException, IllegalAccessException {
        final ErrorHandlerImpl errorHandler = new ErrorHandlerImpl(new LinkedList<>());

        testHandle(new Throwable(), errorHandler, getDefaultHandler(errorHandler),
                NOT_THE_DEFAULT_HANDLER_ERROR_MESSAGE);
    }

    /**
     * Extracts the default {@link ExceptionHandler} from the given {@link ErrorHandlerImpl}
     * (i.e {@link ErrorHandlerImpl#DEFAULT_THROWABLE_HANDLER}).
     *
     * @param errorHandler The {@link ErrorHandlerImpl} from which the default handler must be taken.
     * @return The default {@link ExceptionHandler} of the given {@link ErrorHandlerImpl}.
     * @throws NoSuchFieldException   Never.
     * @throws IllegalAccessException Never.
     */
    private static ExceptionHandler<Throwable, String> getDefaultHandler(ErrorHandlerImpl errorHandler)
            throws NoSuchFieldException, IllegalAccessException {
        final Field defaultHandlerField = errorHandler.getClass().getDeclaredField("DEFAULT_THROWABLE_HANDLER");
        defaultHandlerField.setAccessible(true);
        @SuppressWarnings("unchecked") final ExceptionHandler<Throwable, String> defaultHandler =
                (ExceptionHandler<Throwable, String>) defaultHandlerField.get(errorHandler);
        defaultHandlerField.setAccessible(false);

        return defaultHandler;
    }

    /**
     * Performs testing of handling a given {@link Throwable}, asserting that the {@link ErrorHandler.HandlingResult}
     * are the same when handling from the given {@link ErrorHandler} and from the given {@link ExceptionHandler}.
     *
     * @param throwable        The {@link Throwable} to be handled.
     * @param errorHandler     The {@link ErrorHandler} to be tested.
     * @param throwableHandler The {@link ExceptionHandler} that must handle the given {@code throwable}
     *                         the same way the {@link ErrorHandler}.
     * @param <T>              The concrete subtype of {@link Throwable}.
     */
    private static <T extends Throwable, E> void testHandle(T throwable, ErrorHandler errorHandler,
                                                         ExceptionHandler<T, E> throwableHandler, String errorMessage) {
        final ErrorHandler.HandlingResult errorHandlerResult = errorHandler.handle(throwable);
        final ErrorHandler.HandlingResult exceptionHandlerResult = throwableHandler.handle(throwable);
        // Test a result is returned
        Assert.assertNotNull(errorMessage, errorHandlerResult);
        // Test result content
        Assert.assertEquals(errorMessage,
                exceptionHandlerResult.getHttpErrorCode(),
                errorHandlerResult.getHttpErrorCode());
        Assert.assertEquals(errorMessage,
                exceptionHandlerResult.getErrorRepresentationEntity(),
                errorHandlerResult.getErrorRepresentationEntity());
    }
}
