package pl.touk.camelSpock.annotations;

import org.apache.camel.builder.RouteBuilder;
import org.spockframework.runtime.extension.ExtensionAnnotation;
import pl.touk.camelSpock.CamelExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtensionAnnotation(CamelExtension.class)
public @interface Context {

    String[] ctx() default {};

    Class<? extends RouteBuilder>[] routeBuilders() default {};

}
