package com.bellotapps.utils.error_handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@link Configuration} class in charge of creating an {@link ErrorHandler} bean.
 */
@Configuration
/* package */ class ErrorHandlerCreationConfigurer
        implements ImportAware, InitializingBean, BeanFactoryAware, BeanClassLoaderAware {

    private final static Logger LOGGER = LoggerFactory.getLogger(ErrorHandlerCreationConfigurer.class);

    private final static String ERROR_MESSAGE = "Could not initialize the error handler";

    /**
     * The {@link AnnotationMetadata} from the importing class
     * (used to get the packages to scan and filters to apply to create the {@link ExceptionHandler}s).
     */
    private AnnotationMetadata importMetadata;

    /**
     * The {@link BeanFactory} used to check if there are beans of the scanned {@link ExceptionHandler}s.
     */
    private BeanFactory beanFactory;

    /**
     * {@link ClassLoader} used to get {@link ExceptionHandler} classes.
     */
    private ClassLoader classLoader;

    /**
     * The {@link List} of scanned {@link ExceptionHandler}s.
     */
    private final List<ExceptionHandler<? extends Throwable>> handlers;

    /**
     * Holds the classes that will be used as {@link ExceptionHandler}s.
     */
    private final Set<Class<?>> exceptionHandlerClasses;

    /**
     * Holds the {@link ExceptionHandler} subclasses which does not have spring beans declared.
     */
    private final Set<Class<? extends ExceptionHandler<? extends Throwable>>> noBeanClasses;

    /**
     * Indicates whether the configurer class is initialized.
     */
    private boolean initialized;

    /**
     * Constructor.
     */
    public ErrorHandlerCreationConfigurer() {
        this.handlers = new LinkedList<>();
        exceptionHandlerClasses = new HashSet<>();
        noBeanClasses = new HashSet<>();
        this.initialized = false;
    }

    @Bean
    /* package */ ErrorHandler errorHandler() {
        if (initialized) {
            return new ErrorHandlerImpl(handlers);
        }
        throw new IllegalStateException("The ErrorHandlerCreationConfigurer was not correctly initialized");
    }

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.importMetadata = importMetadata;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final String importingClassName = importMetadata.getClassName();
        LOGGER.info("Using error handler configuration set with annotated class {}", importingClassName);
        initializeExceptionHandlers(importingClassName); // Initializes this configuration class

        this.initialized = true;
    }

    /**
     * Initializes this configuration class by saving the objects to be used as {@link ExceptionHandler}.
     * This objects are saved in the {@code handlers} {@link List}, which will be passed
     * to the {@link ErrorHandlerImpl} that this configuration class is in charge of creating.
     *
     * @param importingClassName The {@link Class} that imported this configuration class.
     * @throws ClassNotFoundException If the given {@code importingClassName} could not be found.
     */
    private void initializeExceptionHandlers(String importingClassName) throws ClassNotFoundException {

        final Class<?> importingClass = Class.forName(importingClassName);
        final EnableErrorHandler enableErrorHandler =
                Optional.ofNullable(AnnotationUtils.findAnnotation(importingClass, EnableErrorHandler.class))
                        .orElseThrow(IllegalArgumentException::new);

        scanPackages(enableErrorHandler); // Scan packages, saving which classes must be used as ExceptionHandlers
        searchForExceptionHandlerBeans(); // Check if there are beans defined for those classes
        instantiateNoBeanHandlers(); // Create handlers for classes that does not have beans declared.
    }


    /**
     * Performs package scanning according to the metadata in the given {@link EnableErrorHandler} annotation.
     *
     * @param enableErrorHandler The {@link EnableErrorHandler} annotation containing the metadata
     *                           to be used for package scanning
     */
    private void scanPackages(final EnableErrorHandler enableErrorHandler) {
        Objects.requireNonNull(enableErrorHandler,
                "The enableErrorHandler annotation object must not be null");
        final String[] basePackages = enableErrorHandler.basePackages();
        final String[] basePackageClasses = Arrays.stream(enableErrorHandler.basePackageClasses())
                .map(Class::getPackage)
                .map(Package::getName)
                .toArray(String[]::new);
        final Set<String> packages = Stream.concat(Arrays.stream(basePackages), Arrays.stream(basePackageClasses))
                .collect(Collectors.toSet());

        LOGGER.debug("Scanning the following packages for ExceptionHandlers: {}", packages);

        // Create classpath scanner to get classes that implement the ExceptionHandler interface
        // and are annotated with ExceptionHandlerObject annotation
        final ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(ExceptionHandlerObject.class));
        scanner.addIncludeFilter(new AssignableTypeFilter(ExceptionHandler.class));

        // Scan packages to get classes used as exception handlers
        // (i.e are located in the packages to scan and, implement ExceptionHandler interface,
        // and  are annotated with the ExceptionHandlerObject annotation)
        final Set<Class<?>> classes = packages.stream()
                .map(scanner::findCandidateComponents)
                .flatMap(Collection::stream)
                .map(beanDef -> ClassUtils.resolveClassName(beanDef.getBeanClassName(), this.classLoader))
                .collect(Collectors.toSet());

        this.exceptionHandlerClasses.addAll(classes);
    }

    /**
     * Tries to get spring beans of the classes contained in the {@code exceptionHandlerClasses} {@link Set}.
     * If a bean is found, it is saved in the {@code handlers} list.
     * Those classes that does not have beans defined will be saved in the {@code noBeanClasses} {@link Set}.
     */
    private void searchForExceptionHandlerBeans() {
        for (Class<?> klass : this.exceptionHandlerClasses) {
            try {
                @SuppressWarnings("unchecked") final Class<? extends ExceptionHandler<? extends Throwable>>
                        handlerClass = (Class<? extends ExceptionHandler<? extends Throwable>>) klass;
                try {
                    final ExceptionHandler<? extends Throwable> handler = beanFactory.getBean(handlerClass);
                    @SuppressWarnings("unchecked") final Class<? extends Throwable> throwableClass =
                            (Class<? extends Throwable>) ResolvableType
                                    .forClass(ExceptionHandler.class, handler.getClass())
                                    .getGeneric(0)
                                    .resolve();
                    LOGGER.info("Found bean of {} for throwable {}",
                            handler.getClass().getName(), throwableClass.getName());
                    this.handlers.add(handler);

                } catch (NoUniqueBeanDefinitionException e) {
                    // More than one bean exist for the given handler class
                    // To continue with initialization, we create one own handler of the given class
                    LOGGER.warn("More than one bean exist for class {}. Will instantiate own handler", handlerClass);
                    noBeanClasses.add(handlerClass);
                } catch (NoSuchBeanDefinitionException e) {
                    // No bean for the given handler class. We create one own handler of the given class.
                    LOGGER.debug("No bean for class {}. Will create one", handlerClass);
                    noBeanClasses.add(handlerClass);
                } catch (BeansException e) {
                    // In this case, a bean exists, but the beanFactory could not get it
                    LOGGER.error("Could not get bean for class {}", handlerClass);
                    throw new BeanInitializationException(ERROR_MESSAGE, e);
                }
            } catch (ClassCastException e) {
                LOGGER.error("Some error occurred", e); // TODO: better error message
                throw new BeanInitializationException(ERROR_MESSAGE, e);
            }
        }
    }

    /**
     * Creates and saves into the {@code handlers} list an instance of each of those {@link ExceptionHandler} subclasses
     * in the {@code noBeanClasses} {@link Set}.
     */
    private void instantiateNoBeanHandlers() {
        final List<ExceptionHandler<? extends Throwable>> createdHandlers = noBeanClasses.stream()
                .map(handlerClass -> {
                    try {
                        final Constructor<? extends ExceptionHandler<? extends Throwable>> constructor = handlerClass
                                .getDeclaredConstructor(); // Get default constructor
                        constructor.setAccessible(true);
                        final ExceptionHandler<? extends Throwable> handler = constructor.newInstance();
                        constructor.setAccessible(false);
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
                }).collect(Collectors.toList());

        this.handlers.addAll(createdHandlers);
    }
}
