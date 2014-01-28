package pl.touk.camelSpock

import org.apache.camel.Message
import org.apache.camel.ProducerTemplate
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.SimpleRegistry
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.FieldInfo
import org.spockframework.runtime.model.MethodInfo
import org.spockframework.runtime.model.SpecInfo
import pl.touk.camelSpock.annotations.Context
import pl.touk.camelSpock.annotations.Endpoint
import pl.touk.camelSpock.annotations.RegistryBean

import java.lang.annotation.Annotation

class CamelExtension implements IAnnotationDrivenExtension{

    DefaultCamelContext camelContext = new DefaultCamelContext();

    CamelInterceptor camelInterceptor;

    @Override
    void visitSpecAnnotation(Annotation annotation, SpecInfo spec) {
        Context context =  annotation as Context
        context.routeBuilders().each {
            camelContext.addRoutes(it.newInstance())
        }
        SimpleRegistry simpleRegistry = new SimpleRegistry()
        camelContext.registry = simpleRegistry
        camelInterceptor = new CamelInterceptor(camelContext,simpleRegistry)
        spec.features.each {
            it.featureMethod.addInterceptor(camelInterceptor)
        }
        addHelperMethods(spec)
    }

    private void addHelperMethods(SpecInfo spec) {
        ProducerTemplate producerTemplate = camelContext.createProducerTemplate()
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

    }

    @Override
    void visitFeatureAnnotation(Annotation annotation, FeatureInfo feature) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    void visitFixtureAnnotation(Annotation annotation, MethodInfo fixtureMethod) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    void visitFieldAnnotation(Annotation annotation, FieldInfo field) {
        if (annotation instanceof RegistryBean) {
            camelInterceptor.fields.add(field)
        }
        if (annotation instanceof Endpoint) {
            camelInterceptor.endpoints.add(field)
        }
    }

    @Override
    void visitSpec(SpecInfo spec) {
    }
}


