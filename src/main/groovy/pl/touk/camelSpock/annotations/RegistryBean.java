package pl.touk.camelSpock.annotations;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import pl.touk.camelSpock.impl.CamelExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If spring configuration is used, bean from context will be injected.
 * Otherwise, value of the field will be put into Camel registry
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@ExtensionAnnotation(CamelExtension.class)
public @interface RegistryBean {

    /**
     * @return name of registry bean
     */
    String value() default "";

}
