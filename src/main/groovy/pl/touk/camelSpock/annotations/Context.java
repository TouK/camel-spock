package pl.touk.camelSpock.annotations;

import org.apache.camel.builder.RouteBuilder;
import org.spockframework.runtime.extension.ExtensionAnnotation;
import pl.touk.camelSpock.impl.CamelExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation marking spec to be run by camel-spock extension
 * Can be used in two ways:
 * <ul>
 *     <li>list of route builder classes is given</li>
 *     <li>list of spring context locations is given</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtensionAnnotation(CamelExtension.class)
public @interface Context {

    /**
     *
     * @return list of locations of spring contexts
     */
    String[] ctx() default {};

    /**
     *
     * @return list of route builder classes
     */
    Class<? extends RouteBuilder>[] routeBuilders() default {};

    /**
     *
     * @return should non-resolvable beans be resolved as direct endpoints
     */
    boolean resolveEndpointsToDirect() default true;

}
