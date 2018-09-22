# Error-Handler [![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0) [![Build Status](https://travis-ci.org/juanmbellini/error-handler.svg?branch=master)](https://travis-ci.org/juanmbellini/error-handler) [![Maven Central](https://img.shields.io/maven-central/v/com.bellotapps.utils/error-handler.svg)](https://repo.maven.apache.org/maven2/com/bellotapps/utils/error-handler/2.0.0-RELEASE/)
A centralized error management system

## Description

This is a library that lets you centralize error management in a spring application.

When using several tools, like Spring Security and Jersey Server, each of those may have different error management systems, making it difficult to keep consistent the error management. For example, Jersey uses the ```ExceptionMapper``` interface, and Spring Security, filters. When an exception is thrown inside a Jersey application, you are forced to handle it there. If it's not handled, it won't propagate. Jersey will throw another exception.

This library lets you define objects that will handle a given exception, and that are accessed through another object (which acts like a facade). This facade object can be accessed in the error management systems of all the used tools.

## Instructions

### Maven

Include the following to your dependency list:

```xml
<dependency>
    <groupId>com.bellotapps.utils</groupId>
    <artifactId>error-handler</artifactId>
    <version>2.0.0-RELEASE</version>
</dependency>
```

### Build from source
```bash
$ git clone https://github.com/juanmbellini/error-handler.git
$ cd error-handler
$ mvn clean package
```

## Usage
The following is an example of usage using Spring Boot.

Bootstrapping class:

```java
package com.bellotapps.error_handler_example.config;

// Imports not listed

/**
 * Bootstrap class.
 */
@SpringBootApplication
@EnableErrorHandler(basePackages = "com.bellotapps.error_handler_example.exception_handlers")
public class Application {

    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class)
                .bannerMode(Banner.Mode.OFF)
                .build().run(args);
    }
	    
    /**
     * Configures an {@link ObjectMapper} enabling and disabling certain
     * {@link SerializationFeature}s and {@link DeserializationFeature}s
     *
     * @return The configured {@link ObjectMapper}.
     */
    @Bean
    public ObjectMapper jacksonObjectMapper() {
        final ObjectMapper om = new ObjectMapper();
        // Serialization
        om.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        // Deserialization
        om.disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT);
        om.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        om.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        
        return om;
    }

    /**
     * Creates a {@link ResourceConfig} in order to configure Jersey's behaviour.
     *
     * @return The {@link ResourceConfig} used to configure Jersey's behaviour.
     */
    @Bean
    public ResourceConfig jerseyConfig() {
        final ResourceConfig jerseyConfig = new ResourceConfig();

        jerseyConfig.register(ThrowableMapper.class)
        registerPackages(jerseyConfig, "com.bellotapps.error_handler_example.controllers");
        jerseyConfig.register(new JacksonJaxbJsonProvider(jacksonObjectMapper(),
                JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS));
    
        return jerseyConfig;
    }
    
    /**
     * Registers the classes annotated with the {@link Provider} annotation in the given {@code packages}.
     * This allows package scanning with Jersey (as currently not supported by library).
     *
     * @param packages The packages containing providers.
     */
    private static void registerPackages(ResourceConfig resourceConfig, String... packages) {
        // Register packages of in app Providers
        final ClassPathScanningCandidateComponentProvider scanner =
	            new ClassPathScanningCandidateComponentProvider(false);
	    scanner.addIncludeFilter(new AnnotationTypeFilter(Provider.class));

        Arrays.stream(packages)
                .map(scanner::findCandidateComponents).flatMap(Collection::stream)
                .map(beanDefinition ->
                        ClassUtils.resolveClassName(beanDefinition.getBeanClassName(), resourceConfig.getClassLoader()))
                .forEach(resourceConfig::register);
    }
}

```

Jersey's ```ExceptionMapper```:

```java
package com.bellotapps.error_handler_example.config;

// Imports not listed

/**
 * The global {@link ExceptionMapper} that is in charge of mapping any {@link Throwable} thrown
 * within the Jersey application, using an {@link ErrorHandler}.
 */
@Provider
@Component
public class ThrowableMapper implements ExceptionMapper<Throwable> {

    /**
     * The {@link ErrorHandler} in charge of transforming an exception into data to be returned in the response.
     */
    private final ErrorHandler errorHandler;

    @Autowired
    public ThrowableMapper(ErrorHandler exceptionHandler) {
        this.errorHandler = exceptionHandler;
    }

    @Override
    public Response toResponse(Throwable exception) {
        final ErrorHandler.HandlingResult result = errorHandler.handle(exception);
        return Response.status(result.getHttpErrorCode())
                .entity(Optional.ofNullable(result.getErrorRepresentationEntity()).orElse(""))
                .build();
    }
}
```

An ```ExceptionHandler```:

```java
package com.bellotapps.error_handler_example.exception_handlers;

// Imports not listed

/**
 * {@link ExceptionHandler} in charge of handling {@link Throwable}.
 */
@ExceptionHandlerObject
public class ThrowableHandler implements ExceptionHandler<Throwable> {

    @Override
    public ErrorHandler.HandlingResult handle(Throwable exception) {
        return new ErrorHandler.HandlingResult(500, null);
    }
}

```
### Custom Configuration

You can also create your own configuration class for the ErrorHandler.

For example, you could extend ```AbstractErrorHandlerCreationConfigurer```, implementing the ```getPackagesCollection()``` to tell the packages to be scanned for ```ExceptionHandler``` objects.

Another way is extending the ```AnnotationErrorHandlerCreationConfigurer``` class, setting which is the annotation from which the system must take metadata to get the packages names. Also, you must implement the method from which that metadata is extracted from the annotation.

Finally, you could create your own ```ErrorHandler``` using an ```ErrorHandlerFactory```. Note that you can get a bean of this factory by using the ```@EnableErrorHandlerFactory``` annotation in a ```@Configuration``` class.

## License

Copyright 2018 BellotApps

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 