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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Configuration class extended from {@link AnnotationErrorHandlerCreationConfigurer},
 * using {@link EnableErrorHandler} annotation.
 */
@Configuration
@EnableErrorHandlerFactory
public class ErrorHandlerCreationConfigurer extends AnnotationErrorHandlerCreationConfigurer<EnableErrorHandler> {

    /**
     * Constructor.
     *
     * @param errorHandlerFactory The {@link ErrorHandlerFactory} to be used to create the {@link ErrorHandler} bean.
     */
    @Autowired
    public ErrorHandlerCreationConfigurer(final ErrorHandlerFactory errorHandlerFactory) {
        super(errorHandlerFactory);
    }

    @Override
    protected Class<EnableErrorHandler> getAnnotationClass() {
        return EnableErrorHandler.class;
    }

    @Override
    protected Collection<String> getPackagesCollectionFromAnnotation(final EnableErrorHandler enableErrorHandler) {
        final String[] basePackages = enableErrorHandler.basePackages();
        final String[] basePackageClasses = Arrays.stream(enableErrorHandler.basePackageClasses())
                .map(Class::getPackage)
                .map(Package::getName)
                .toArray(String[]::new);
        return Stream.concat(Arrays.stream(basePackages), Arrays.stream(basePackageClasses))
                .collect(Collectors.toSet());
    }
}
