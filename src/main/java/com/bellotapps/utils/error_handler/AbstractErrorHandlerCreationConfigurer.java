package com.bellotapps.utils.error_handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Collection;
import java.util.HashSet;

/**
 * Abstract configuration class for {@link ErrorHandler} creation.
 */
public abstract class AbstractErrorHandlerCreationConfigurer implements ImportAware, InitializingBean {

    /**
     * The {@link Logger} object.
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractErrorHandlerCreationConfigurer.class);

    /**
     * The {@link AnnotationMetadata} from the importing class
     * (used to get the packages to scan and filters to apply to create the {@link ExceptionHandler}s).
     */
    private AnnotationMetadata importMetadata;

    /**
     * The {@link ErrorHandlerFactory} to be used to create the {@link ErrorHandler} bean.
     */
    private final ErrorHandlerFactory errorHandlerFactory;

    /**
     * A {@link Collection} of packages names to be scanned in search of {@link ExceptionHandler}s.
     */
    private final Collection<String> packagesCollection;

    /**
     * Indicates whether the configurer class is initialized.
     */
    private boolean initialized;

    /**
     * Protected constructor, in order to let it be initialized only by subclasses.
     *
     * @param errorHandlerFactory The {@link ErrorHandlerFactory} to be used to create the {@link ErrorHandler} bean.
     */
    protected AbstractErrorHandlerCreationConfigurer(ErrorHandlerFactory errorHandlerFactory) {
        this.errorHandlerFactory = errorHandlerFactory;
        this.packagesCollection = new HashSet<>();
        this.initialized = false;
    }

    /**
     * @return The {@link AnnotationMetadata} set in this configurer class.
     */
    protected AnnotationMetadata getImportMetadata() {
        if (!initialized || importMetadata == null) {
            return importMetadata;
        }
        throw new IllegalStateException("The ErrorHandlerCreationConfigurer was not correctly initialized");
    }

    @Bean
    /* package */ ErrorHandler errorHandler() {
        if (initialized && errorHandlerFactory != null) {
            return errorHandlerFactory.createErrorHandler(packagesCollection);
        }
        throw new IllegalStateException("The ErrorHandlerCreationConfigurer was not correctly initialized");
    }

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.importMetadata = importMetadata;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        LOGGER.info("Starting ErrorHandler configuration, imported by class {}", importMetadata.getClassName());
        initializePackagesCollection();
        this.initialized = true;
    }

    /**
     * Fills the {@code packagesCollection}
     * with the packages extracted from the {@link EnableErrorHandler} annotation
     * that was used to import this {@link Configuration} class.
     */
    private void initializePackagesCollection() throws Exception {
        this.packagesCollection.addAll(getPackagesCollection());
    }

    /**
     * Returns a {@link Collection} of package names to be used by the {@link ErrorHandlerFactory}
     * to create the {@link ErrorHandler}.
     *
     * @return The {@link Collection} of packages names.
     * @throws Exception In case some error occurs in this process.
     */
    protected abstract Collection<String> getPackagesCollection() throws Exception;
}
