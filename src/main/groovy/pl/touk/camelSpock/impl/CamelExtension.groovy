package pl.touk.camelSpock.impl

import org.apache.camel.CamelContext
import org.apache.camel.ExchangePattern
import org.apache.camel.Message
import org.apache.camel.Processor
import org.apache.camel.ProducerTemplate
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.SimpleRegistry
import org.apache.camel.spi.Registry
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.FieldInfo
import org.spockframework.runtime.model.MethodInfo
import org.spockframework.runtime.model.SpecInfo
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import pl.touk.camelSpock.annotations.Context
import pl.touk.camelSpock.annotations.Endpoint
import pl.touk.camelSpock.annotations.RegistryBean

import java.lang.annotation.Annotation

class CamelExtension implements IAnnotationDrivenExtension{

    CamelContext camelContext;

    CamelInterceptor camelInterceptor;

    @Override
    void visitSpecAnnotation(Annotation annotation, SpecInfo spec) {
        Context context =  annotation as Context
        SimpleRegistry simpleRegistry = null
        Registry registry = null
        if (context.routeBuilders().length > 0) {
            camelContext = new DefaultCamelContext()
            context.routeBuilders().each {
                camelContext.addRoutes(it.newInstance())
            }
            simpleRegistry = new SimpleRegistryWithDefaults(camelContext: camelContext,
                    resolveRefEndpoints: context.resolveEndpointsToDirect())
            camelContext.setRegistry(simpleRegistry)
        } else {
          ApplicationContext ctx = new ClassPathXmlApplicationContext(context.ctx())
          camelContext = ctx.getBean(CamelContext)
          registry = camelContext.registry
        }
        camelInterceptor = new CamelInterceptor(camelContext,simpleRegistry,registry, spec)
        spec.features.each {
            it.featureMethod.addInterceptor(camelInterceptor)
        }
        addHelperMethods(spec)
    }

    private void addHelperMethods(SpecInfo spec) {
        ProducerTemplate producerTemplate = camelContext.createProducerTemplate()

        def sendWithExchange = { ExchangePattern pattern, String target, Object body ->
            producerTemplate.send(target,pattern, { it.in.body = body} as Processor)
        }

        spec.reflection.metaClass.inOut = sendWithExchange.curry(ExchangePattern.InOut)
        spec.reflection.metaClass.inOnly = sendWithExchange.curry(ExchangePattern.InOnly)
        spec.reflection.metaClass.send = producerTemplate.&send
        spec.reflection.metaClass.sendBody = producerTemplate.&sendBody
        spec.reflection.metaClass.request = producerTemplate.&request
        spec.reflection.metaClass.requestBody = producerTemplate.&requestBody
        spec.reflection.metaClass.requestXml = {
            String endpoint, Object body ->
                new XmlSlurper().parseText(requestBody(endpoint,body,String))
        }
        Message.metaClass.getXml = {
            new XmlSlurper().parseText(getBody(String))
        }
        spec.reflection.metaClass.getEndpoint  = camelContext.&getEndpoint

    }

    @Override
    void visitFeatureAnnotation(Annotation annotation, FeatureInfo feature) {
    }

    @Override
    void visitFixtureAnnotation(Annotation annotation, MethodInfo fixtureMethod) {
    }

    @Override
    void visitFieldAnnotation(Annotation annotation, FieldInfo field) {
        if (annotation instanceof RegistryBean) {
            camelInterceptor.fields.add(field)
        }
        if (annotation instanceof Endpoint) {
            camelInterceptor.mockEndpoints.add(field)
        }
    }

    @Override
    void visitSpec(SpecInfo spec) {
    }

}

private class SimpleRegistryWithDefaults extends SimpleRegistry {

    CamelContext camelContext
    boolean resolveRefEndpoints

    @Override
    def <T> T lookup(String name, Class<T> type) {
        T obj = super.lookup(name, type)
        if (!obj && resolveRefEndpoints && org.apache.camel.Endpoint.isAssignableFrom(type)) {
            obj = camelContext.getEndpoint("direct:"+name) as T
        }
        obj
    }
}


