package pl.touk.camelSpock.annotations;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import pl.touk.camelSpock.impl.CamelExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Camel endpoint will be injected into spec fields with this annotation.
 * If configuration without spring is used, we assume it'll be mock endpoint
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@ExtensionAnnotation(CamelExtension.class)
public @interface Endpoint {

    /**
     * @return name of endpoint to be injected, if empty we assume field name is name of the endpoint
     */
    String value() default "";

}
