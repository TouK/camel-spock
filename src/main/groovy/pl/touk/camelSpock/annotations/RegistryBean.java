package pl.touk.camelSpock.annotations;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import pl.touk.camelSpock.impl.CamelExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@ExtensionAnnotation(CamelExtension.class)
public @interface RegistryBean {

    String value() default "";
}
