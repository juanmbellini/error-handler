package com.bellotapps.utils.error_handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.ResolvableType;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Object in charge of creating an {@link ErrorHandler}.
 */
public class ErrorHandlerFactory {

    /**
     * The {@link Logger} object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandlerFactory.class);

    /**
     * An error message to be used when something goes wrong when trying to create the error handler.
     */
    private final static String ERROR_MESSAGE = "Could not create an error handler";

    /**
     * {@link ClassLoader} used to get {@link ExceptionHandler} classes in package scanning phase.
     */
    private final ClassLoader classLoader;

    /**
     * The {@link BeanFactory} used to check if there are beans of the scanned {@link ExceptionHandler}s.
     * This is used in order to use beans instead of own created {@link ExceptionHandler}s.
     */
    private final BeanFactory beanFactory;

    /**
     * A {@link ClassPathScanningCandidateComponentProvider}
     * used for scanning packages in search of classes definitions.
     */
    private final ClassPathScanningCandidateComponentProvider scanner;

    /**
     * A {@link Map} holding cached {@link ExceptionHandler}s for a given package name.
     */
    private final Map<String, List<ExceptionHandler<? extends Throwable, ?>>> cachedHandlers;


    /**
     * Constructor.
     *
     * @param classLoader The {@link ClassLoader} used to scan packages.
     * @param beanFactory The {@link BeanFactory} used to get beans (if they exists)
     *                    of the scanned {@link ExceptionHandler}s
     */
    public ErrorHandlerFactory(ClassLoader classLoader, BeanFactory beanFactory) {
        this.classLoader = classLoader;
        this.beanFactory = beanFactory;
        this.scanner = new ClassPathScanningCandidateComponentProvider(false);
        // Scan for classes implementing ExceptionHandler interface, and annotated with ExceptionHandlerObject.
        this.scanner.addIncludeFilter(new ExceptionHandlerObjectAnnotatedAndExceptionHandlerAssignableTypeFilter());
        this.cachedHandlers = new ConcurrentHashMap<>();
    }


    /**
     * Clears the cache stored in this factory
     * (i.e will make it perform package scanning again when asking for an error handler).
     */
    public void resetCache() {
        this.cachedHandlers.clear();
    }

    /**
     * Clears the cache for the given {@code packages}.
     *
     * @param packages The packages whose cache will be cleared.
     */
    public void resetCache(String... packages) {
        resetCache(Arrays.asList(packages));
    }

    /**
     * Clears the cache for the given {@code packages}.
     *
     * @param packages The packages whose cache will be cleared.
     */
    public void resetCache(Collection<String> packages) {
        packages.forEach(this.cachedHandlers::remove);
    }

    /**
     * Creates an {@link ErrorHandler}, scanning for {@link ExceptionHandler} in the given {@code packages}.
     *
     * @param packages The packages to be scanned for {@link ExceptionHandler}s.
     * @return The created {@link ErrorHandler}.
     * @see ExceptionHandlerObject
     * @see ExceptionHandler
     */
    public ErrorHandler createErrorHandler(String... packages) {
        return createErrorHandler(Arrays.asList(packages));
    }

    /**
     * Creates an {@link ErrorHandler}, scanning for {@link ExceptionHandler} in the given {@code packages}.
     *
     * @param packages The packages to be scanned for {@link ExceptionHandler}s.
     * @return The created {@link ErrorHandler}.
     * @see ExceptionHandlerObject
     * @see ExceptionHandler
     */
    public ErrorHandler createErrorHandler(Collection<String> packages) {
        // Perform package scanning for those not cached
        final Map<String, List<ExceptionHandler<? extends Throwable, ?>>> foundedHandlers = packages.stream()
                .filter(pkg -> !cachedHandlers.containsKey(pkg))
                .collect(Collectors.toMap(Function.identity(),
                        pkg -> scanPackage(pkg)
                                .stream()
                                .map(klass -> new ExceptionHandlerGetter<>(klass, beanFactory))
                                .map(ExceptionHandlerGetter::getHandler)
                                .collect(Collectors.toList())));
        // Save in cache those handlers that have been found
        this.cachedHandlers.putAll(foundedHandlers);
        // Get stored handlers
        final List<ExceptionHandler<? extends Throwable, ?>> handlers = cachedHandlers.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        // Create the new ErrorHandler
        return new ErrorHandlerImpl(handlers);
    }

    /**
     * Scans the given package, searching for {@link ExceptionHandler}s in it (according to the {@code scanner}).
     *
     * @param pkg The package to be scanned.
     * @return The classes founded in the given package
     * matching the restrictions to be a valid {@link ExceptionHandler}.
     * @see ExceptionHandlerObject
     * @see ExceptionHandler
     */
    private Set<Class<?>> scanPackage(String pkg) {
        return this.scanner.findCandidateComponents(pkg)
                .stream()
                .map(beanDef -> ClassUtils.resolveClassName(beanDef.getBeanClassName(), this.classLoader))
                .collect(Collectors.toSet());
    }

    /**
     * Helper class to get extensions of {@link ExceptionHandler} of a given {@link Throwable}, in a type-safe way.
     *
     * @param <T> The concrete type of {@link ExceptionHandler}.
     */
    private final static class ExceptionHandlerGetter<T extends ExceptionHandler<? extends Throwable, ?>> {

        /**
         * The class of {@link ExceptionHandler} extension.
         */
        private final Class<T> handlerClass;

        /**
         * The {@link BeanFactory} used to get a bean of the given {@code handlerClass}.
         */
        private final BeanFactory beanFactory;

        /**
         * Constructor.
         *
         * @param handlerClass The class of {@link ExceptionHandler} extension.
         * @param beanFactory  The {@link BeanFactory} used to get a bean of the given {@code handlerClass}.
         */
        private ExceptionHandlerGetter(Class<?> handlerClass, BeanFactory beanFactory) {
            //noinspection unchecked
            this.handlerClass = (Class<T>) handlerClass;
            this.beanFactory = beanFactory;
        }


        /**
         * Retrieves an {@link ExceptionHandler} of the {@link Class} of {@link ExceptionHandler}
         * this {@link ExceptionHandlerGetter} was created for,
         * trying to get a spring bean or instantiating it
         *
         * @return An {@link ExceptionHandler} of the given {@link Class}.
         */
        private T getHandler() {
            return searchForBean().orElse(instantiate());
        }


        /**
         * Tries to get a spring bean for the {@link Class} of {@link ExceptionHandler}
         * this {@link ExceptionHandlerGetter} was created for.
         * If a bean is found, it will be wrapped in the returned {@link Optional}.
         * Otherwise, the {@link Optional} will be empty.
         * In case of unexpected errors, a {@link BeanInitializationException} will be thrown.
         *
         * @return An {@link Optional} containing the found bean,
         * or empty in case no bean (or multiple beans) were found.
         * @throws BeanInitializationException In case some unexpected error occurred when searching for the bean.
         */
        private Optional<T> searchForBean()
                throws BeanInitializationException {
            try {
                final T handler = beanFactory.getBean(handlerClass);
                logBeanFound(handlerClass);
                return Optional.of(handler);
            } catch (NoUniqueBeanDefinitionException e) {
                // More than one bean exist for the given handler class, so we create our own as we don't know which to use.
                logMultipleBeans(handlerClass);
                return Optional.empty();
            } catch (NoSuchBeanDefinitionException e) {
                // No bean for the given handler class, so we create our own handler of the given class.
                logNoBeanFound(handlerClass);
                return Optional.empty();
            } catch (BeansException e) {
                logBeansException(handlerClass);
                throw new BeanInitializationException(ERROR_MESSAGE, e);
            } catch (Throwable e) {
                logUnexpectedErrorWhenSearchingForBeans(handlerClass, e);
                throw new BeanInitializationException(ERROR_MESSAGE, e);
            }
        }

        /**
         * Creates an instance of the {@link Class} of {@link ExceptionHandler}
         * this {@link ExceptionHandlerGetter} was created for.
         */
        private T instantiate() {
            try {
                final Constructor<T> constructor = handlerClass.getDeclaredConstructor(); // Get default constructor
                final boolean accessible = constructor.isAccessible(); // First, check the accessible flag
                constructor.setAccessible(true); // Make it accessible (if already was, nothing happens)
                final T handler = constructor.newInstance(); // Instantiate
                constructor.setAccessible(accessible); // Restore the accessible flag
                return handler;
            } catch (NoSuchMethodException e) {
                LOGGER.error("No default constructor for class {}", handlerClass);
                throw new BeanInitializationException(ERROR_MESSAGE, e);
            } catch (InstantiationException e) {
                LOGGER.error("Could not instantiate class {}. Is this class abstract?", handlerClass);
                throw new BeanInitializationException(ERROR_MESSAGE, e);
            } catch (IllegalAccessException e) {
                LOGGER.debug("Could not access default constructor of class {}", handlerClass);
                LOGGER.error("Could not instantiate class {}", handlerClass);
                throw new BeanInitializationException(ERROR_MESSAGE, e);
            } catch (InvocationTargetException e) {
                LOGGER.error("Could not instantiate exception handler {} as an exception was thrown " +
                        "while executing its constructor.", handlerClass);
                throw new BeanInitializationException(ERROR_MESSAGE, e);
            }
        }


        /**
         * Logs that a bean of the given {@link ExceptionHandler} class has bean found.
         *
         * @param handlerClass The class of the bean that has been found.
         */
        private static void logBeanFound(Class<? extends ExceptionHandler<? extends Throwable, ?>> handlerClass) {
            @SuppressWarnings("unchecked") final Class<? extends Throwable> throwableClass =
                    (Class<? extends Throwable>) ResolvableType.forClass(ExceptionHandler.class, handlerClass)
                            .getGeneric(0)
                            .resolve();
            LOGGER.info("Found bean of {} for throwable {}", handlerClass.getName(), throwableClass.getName());
        }

        /**
         * Logs that several beans of the given {@link ExceptionHandler} class has bean found.
         *
         * @param handlerClass The class of the beans that has been found.
         */
        private static void logMultipleBeans(Class<? extends ExceptionHandler<? extends Throwable, ?>> handlerClass) {
            LOGGER.warn("More than one bean exist for class {}. Will instantiate own handler", handlerClass);
        }

        /**
         * Logs that no bean was found of the given {@link ExceptionHandler} class.
         *
         * @param handlerClass The class of the not found beans.
         */
        private static void logNoBeanFound(Class<? extends ExceptionHandler<? extends Throwable, ?>> handlerClass) {
            LOGGER.debug("No bean for class {}. Will create one", handlerClass);
        }

        /**
         * Logs that there were errors when trying to find a bean of the given {@link ExceptionHandler} class,
         * so it was not possible to get one of it.
         *
         * @param handlerClass The class of the bean that could not be gotten due to errors.
         */
        private static void logBeansException(Class<? extends ExceptionHandler<? extends Throwable, ?>> handlerClass) {
            LOGGER.error("Could not get bean for class {}", handlerClass);

        }

        /**
         * Logs that there were an unexpected error when trying to get a bean.
         *
         * @param klass The {@link Class} for which an unexpected error occurred when trying to get a bean for it.
         * @param e     The {@link Throwable} that was thrown in the process.
         */
        private static void logUnexpectedErrorWhenSearchingForBeans(Class<?> klass, Throwable e) {
            LOGGER.error("Some unexpected error occurred when trying to get an Exception Handler for class {}.", klass);
            LOGGER.error("Please, report this issue.", e);
        }
    }

    /**
     * Custom filter to get classes annotated with {@link ExceptionHandlerObject},
     * and that implement the {@link ExceptionHandler} interface.
     */
    private static class ExceptionHandlerObjectAnnotatedAndExceptionHandlerAssignableTypeFilter implements TypeFilter {

        /**
         * Annotation type filter to get those classes annotated with {@link ExceptionHandlerObject}.
         */
        private final AnnotationTypeFilter exceptionHandlerObjectAnnotationFilter;

        /**
         * Assignable type filter to get those classes that implement {@link ExceptionHandler} interface.
         */
        private final AssignableTypeFilter assignableFromExceptionHandlerInterfaceFilter;

        /**
         * Constructor.
         */
        private ExceptionHandlerObjectAnnotatedAndExceptionHandlerAssignableTypeFilter() {
            this.exceptionHandlerObjectAnnotationFilter = new AnnotationTypeFilter(ExceptionHandlerObject.class);
            this.assignableFromExceptionHandlerInterfaceFilter = new AssignableTypeFilter(ExceptionHandler.class);
        }

        @Override
        public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
                throws IOException {
            return exceptionHandlerObjectAnnotationFilter.match(metadataReader, metadataReaderFactory)
                    && assignableFromExceptionHandlerInterfaceFilter.match(metadataReader, metadataReaderFactory);
        }
    }
}
