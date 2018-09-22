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

import java.lang.annotation.*;

/**
 * Indicates that an object is an
 * {@link com.bellotapps.utils.error_handler.ExceptionHandler}.
 * <p>
 * Note that just using the annotation is not enough for being registered in the
 * {@link com.bellotapps.utils.error_handler.ErrorHandler}.
 * The annotated class must also implement the interface.
 *
 * @author Juan Marcos Bellini
 * @see ExceptionHandler
 * @see ErrorHandler
 * @see EnableErrorHandler
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExceptionHandlerObject {
}
