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

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Enables the error handling system.
 *
 * @author Juan Marcos Bellini
 * @see ErrorHandler
 * @see ExceptionHandler
 * @see ExceptionHandlerObject
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({ErrorHandlerCreationConfigurer.class,})
public @interface EnableErrorHandler {

    /**
     * Alias for the {@link #basePackages()} attribute. Allows for more concise annotation declarations e.g.:
     * {@code @EnableErrorHandler("org.my.pkg")} instead of {@code @EnableErrorHandler(basePackages="org.my.pkg")}.
     *
     * @return The same as {@link #basePackages()}.
     */
    @AliasFor("basePackages")
    String[] value() default {};

    /**
     * Base packages to scan for annotated {@link ExceptionHandler}.
     * {@link #value()} is an alias for (and mutually exclusive with) this
     * attribute. Use {@link #basePackageClasses()} for a type-safe alternative to String-based package names.
     *
     * @return The packages names set in the annotation as base packages.
     */
    @AliasFor("value")
    String[] basePackages() default {};

    /**
     * Type-safe alternative to {@link #basePackages()}
     * for specifying the packages to scan for annotated {@link ExceptionHandler}.
     * The package of each class specified will be scanned.
     * Consider creating a special no-op marker class or interface in
     * each package that serves no purpose other than being referenced by this attribute.
     *
     * @return The classes set in the annotation as base package classes.
     */
    Class<?>[] basePackageClasses() default {};
}
