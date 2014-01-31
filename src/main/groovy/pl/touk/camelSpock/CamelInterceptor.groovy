package pl.touk.camelSpock

import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.apache.camel.component.mock.MockEndpoint
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.SimpleRegistry
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FieldInfo
import org.spockframework.runtime.model.SpecInfo
import pl.touk.camelSpock.annotations.Endpoint
import pl.touk.camelSpock.annotations.RegistryBean

import java.lang.reflect.Method

class CamelInterceptor implements IMethodInterceptor{

    DefaultCamelContext camelContext;

    SpecInfo specInfo;

    CamelInterceptor(DefaultCamelContext context,SimpleRegistry registry, SpecInfo spec) {
        this.registry = registry
        this.camelContext = context
        this.specInfo = spec
    }

    SimpleRegistry registry;

    List<FieldInfo> fields = []

    List<FieldInfo> mockEndpoints = []

    @Override
    void intercept(IMethodInvocation invocation) throws Throwable {
        Object spec = invocation.target
        fields.each { field ->
            String name = field.getAnnotation(RegistryBean).value()
            if (name == "") name = field.name
            registry[name] = field.readValue(spec)
        }
        mockEndpoints.each { endpoint ->
            String name = endpoint.getAnnotation(Endpoint).value()
            if (name == "") name = endpoint.name
            MockEndpoint mockEndpoint = camelContext.getEndpoint("mock:${name}",MockEndpoint)
            registry[name] = mockEndpoint
            //??
            CamelMock camelMock = endpoint.readValue(spec) as CamelMock
            mockEndpoint.whenAnyExchangeReceived({ Exchange ex ->
                camelMock.receive(ex)
                Object ret = camelMock.receiveBody(ex.in.body)
                if (ret != null) {
                    ex.in.body = ret
                }
            } as Processor)

        }
        customizeContext(spec)
        customizeRegistry(spec)
        if (!camelContext.isStarted())
            camelContext.start()
        invocation.proceed()
    }

    def customizeRegistry(Object spec) {
        Method method = spec.class.methods.find { it.name == "prepareRegistry" && it.parameterTypes == [SimpleRegistry] }
        method?.invoke(spec, registry)
    }

    def customizeContext(Object spec) {
        Method method = spec.class.methods.find { it.name == "prepareContext" && it.parameterTypes == [CamelContext] }
        method?.invoke(spec, registry)
    }


}
