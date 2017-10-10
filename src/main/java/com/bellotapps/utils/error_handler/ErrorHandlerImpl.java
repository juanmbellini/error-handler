package com.bellotapps.utils.error_handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.ResolvableType;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Concrete implementation of {@link ErrorHandler}.
 */
/* package */ class ErrorHandlerImpl implements ErrorHandler, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandlerContainer.class);

    /**
     * Set of {@link ExceptionHandlerContainer}s.
     */
    private final Set<ExceptionHandlerContainer> handlers;

    /**
     * Default {@link ExceptionHandler}, in case no one is set for {@link Throwable} (i.e fallback handler).
     */
    private static final ExceptionHandler<Throwable> DEFAULT_THROWABLE_HANDLER =
            (ignored) -> new HandlingResult(500, null);


    /**
     * Constructor.
     *
     * @param handlers The {@link List} of {@link ExceptionHandler} that will be used to handle exceptions.
     */
    /* package */ ErrorHandlerImpl(final List<ExceptionHandler<? extends Throwable>> handlers) {
        final Set<ExceptionHandlerContainer> container = toContainers(handlers);

        // Check if there is an ExceptionHandlerContainer for Throwable
        final long throwableCount = container.stream()
                .map(ExceptionHandlerContainer::getExceptionClass)
                .filter(klass -> klass.equals(Throwable.class))
                .count();
        if (throwableCount == 0) {
            LOGGER.warn("No ExceptionHandler defined for Throwable. Using default.");
            //noinspection unchecked
            container.add(new ExceptionHandlerContainer(DEFAULT_THROWABLE_HANDLER));
        }

        this.handlers = Collections.unmodifiableSet(container); // Make sure the set does not change never.
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        LOGGER.info("Error handler initialized");
        LOGGER.debug("Will handle {}", this.handlers.stream()
                .map(ExceptionHandlerContainer::getExceptionClass)
                .collect(Collectors.toSet()));
    }

    @Override
    public <T extends Throwable> HandlingResult handle(T exception) {
        Objects.requireNonNull(exception, "The exception must not be null");

        //noinspection unchecked
        final Class<T> receivedExceptionClass = (Class<T>) exception.getClass();
        //noinspection unchecked
        final ExceptionHandler<T> handler = this.handlers.stream()
                .filter(container -> container.getExceptionClass().isAssignableFrom(receivedExceptionClass))
                .map(container -> new ContainerWithDistance(receivedExceptionClass, container))
                .min(Comparator.comparingInt(ContainerWithDistance::getDistance))
                .map(ContainerWithDistance::getContainer)
                .orElseThrow(() -> {
                    LOGGER.error("No container saved for received exception, which is a throwable");
                    return new IllegalStateException("No container saved for received exception, which is a throwable."
                            + " Consider storing a ExceptionHandlerContainer for Throwable?");
                })
                .getHandler();

        return handler.handle(exception);
    }


    /**
     * Maps the given {@link List} of {@link ExceptionHandler} into a {@link Set} of {@link ExceptionHandlerContainer}.
     * Note that there will be only one {@link ExceptionHandlerContainer} for each subtype of {@link Throwable}.
     *
     * @param handlers The {@link List} of {@link ExceptionHandler} to be mapped into {@link ExceptionHandlerContainer}.
     * @return A {@link Set} holding the {@link ExceptionHandlerContainer}
     * that result from the given {@link List} of {@link ExceptionHandler}
     */
    private static Set<ExceptionHandlerContainer> toContainers(List<ExceptionHandler<? extends Throwable>> handlers) {
        // Transform the list of ExceptionHandlers into a list of ExceptionHandlerContainers
        final List<ExceptionHandlerContainer> containers = handlers.stream()
                .map((Function<ExceptionHandler<? extends Throwable>, ExceptionHandlerContainer>)
                        ExceptionHandlerContainer::new)
                .collect(Collectors.toList());

        // Check if there is more than one container holding the same class
        //noinspection unchecked
        final Map<Class<? extends Throwable>, List<ExceptionHandlerContainer>> repeated =
                containers.stream()
                        .collect(Collectors.groupingBy(ExceptionHandlerContainer::getExceptionClass))
                        .entrySet().stream()
                        .filter(entry -> entry.getValue().size() > 1)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // Leave one container for each class of throwable for those that there were repeated
        repeated.forEach((exceptionClass, containersList) -> {
            if (containersList.size() == 0) {
                return; // This does not happen, but just in case...
            }
            final ExceptionHandlerContainer firstContainer = containersList.get(0);
            containers.removeAll(containersList); // Remove all those containers that hold the same throwable class
            //noinspection unchecked
            containers.add(containersList.get(0)); // Save just the first one in the list
            LOGGER.warn("More than one ExceptionHandler for Throwable {}. {} Will be used.",
                    exceptionClass, firstContainer.getHandler());
        });

        return new HashSet<>(containers);
    }

    /**
     * Calculates the distance that exists from the given {@code receivedExceptionClass}
     * to the given {@code savedExceptionClass} in the class hierarchy.
     *
     * @param receivedExceptionClass The {@link Class} whose distance to the given {@code savedExceptionClass}
     *                               must be calculated.
     * @param savedExceptionClass    The {@link Class} to which the distance must be calculated.
     * @return The distance between the two classes.
     * @throws NullPointerException     If any of both classes is null.
     * @throws IllegalArgumentException If the given {@code receivedExceptionClass}
     *                                  is not a subclass of the given {@code savedExceptionClass}.
     */
    private static int distance(Class<?> receivedExceptionClass, final Class<?> savedExceptionClass)
            throws NullPointerException, IllegalArgumentException {
        Objects.requireNonNull(receivedExceptionClass, "Received null as exception to handle");
        Objects.requireNonNull(savedExceptionClass, "A null was saved as a managed exception");
        if (!savedExceptionClass.isAssignableFrom(receivedExceptionClass)) {
            throw new IllegalArgumentException("Received exception class is not assignable from saved exception class");
        }
        int distance = 0;
        while (receivedExceptionClass != savedExceptionClass) {
            distance++;
            receivedExceptionClass = receivedExceptionClass.getSuperclass();
        }

        return distance;
    }


    /**
     * Container class that holds a {@link Class} of object that extends {@link Throwable},
     * together with a {@link Function} that receives the said object and returns an
     * {@link HandlingResult} (i.e an exception handler).
     *
     * @param <T> The concrete subtype of {@link Throwable}.
     */
    private static final class ExceptionHandlerContainer<T extends Throwable> {

        /**
         * The {@link Throwable} subtype class.
         */
        private final Class<T> exceptionClass;

        /**
         * The {@link ExceptionHandler} in charge of handling the {@link Throwable} of type {@code T}.
         */
        private final ExceptionHandler<T> handler;


        /**
         * Constructor.
         *
         * @param handler The {@link ExceptionHandler} in charge of handling the {@link Throwable} of type {@code T}.
         */
        private ExceptionHandlerContainer(ExceptionHandler<T> handler) {
            Objects.requireNonNull(handler, "The handler must not be null");
            //noinspection unchecked
            this.exceptionClass = (Class<T>) ResolvableType.forClass(ExceptionHandler.class, handler.getClass())
                    .getGeneric(0)
                    .resolve();
            this.handler = handler;
        }

        /**
         * @return The {@link Throwable} subtype class.
         */
        private Class<T> getExceptionClass() {
            return exceptionClass;
        }

        /**
         * @return The {@link ExceptionHandler} in charge of handling the {@link Throwable} of type {@code T}.
         */
        private ExceptionHandler<T> getHandler() {
            return handler;
        }

        /**
         * Equals based on {@code exceptionClass}.
         * Two objects are the same {@link ExceptionHandlerContainer} if they are both instances of the said class,
         * and the {@code exceptionClass} for both are equal.
         *
         * @param o The object to be compared with.
         * @return {@code true} if they are the same, or {@code false} otherwise.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ExceptionHandlerContainer)) return false;

            ExceptionHandlerContainer<?> that = (ExceptionHandlerContainer<?>) o;

            return exceptionClass.equals(that.exceptionClass);
        }

        /**
         * @return The hashcode, based on {@code exceptionClass}.
         */
        @Override
        public int hashCode() {
            return exceptionClass.hashCode();
        }
    }

    /**
     * Container class that holds a {@link ExceptionHandlerContainer} together with the distance from the
     * {@link Throwable} class in that container to a given subclass of {@link Throwable} in the class hierarchy.
     * This container is used to avoid recalculating the distance each time two classes must be compared.
     *
     * @param <T> The concrete subtype of {@link Throwable}.
     */
    private final static class ContainerWithDistance<T extends Throwable> {

        /**
         * The distance from a given subclass of {@link Throwable}
         * to the {@link Throwable} held in the {@code container.}
         */
        private final int distance;

        /**
         * The {@link ExceptionHandlerContainer} that holds a {@link Throwable} to which the distance must be held.
         */
        private final ExceptionHandlerContainer<T> container;

        /**
         * Constructor.
         *
         * @param exceptionClass The subclass of {@link Throwable} from which the distance must be calculated and held.
         * @param container      The {@link ExceptionHandlerContainer} that holds the {@link Throwable} superclass.
         */
        private ContainerWithDistance(Class<T> exceptionClass, ExceptionHandlerContainer<T> container) {
            this.distance = distance(exceptionClass, container.getExceptionClass());
            this.container = container;
        }

        /**
         * @return The distance from a given subclass of {@link Throwable}
         * to the {@link Throwable} held in the {@code container.}
         */
        private int getDistance() {
            return distance;
        }

        /**
         * @return The {@link ExceptionHandlerContainer} that holds a {@link Throwable}
         * to which the distance must be held.
         */
        private ExceptionHandlerContainer<T> getContainer() {
            return container;
        }
    }
}
