package pl.touk.camelSpock

import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.apache.camel.component.mock.MockEndpoint
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.SimpleRegistry
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FieldInfo
import pl.touk.camelSpock.annotations.Endpoint
import pl.touk.camelSpock.annotations.RegistryBean

class CamelInterceptor implements IMethodInterceptor{

    DefaultCamelContext camelContext;

    CamelInterceptor(DefaultCamelContext context,SimpleRegistry registry) {
        this.registry = registry
        this.camelContext = context
    }

    SimpleRegistry registry;

    List<FieldInfo> fields = []

    List<FieldInfo> endpoints = []

    @Override
    void intercept(IMethodInvocation invocation) throws Throwable {
        Object spec = invocation.target
        fields.each { field ->
            String name = field.getAnnotation(RegistryBean).value()
            if (name == "") name = field.name
            registry[name] = field.readValue(spec)
        }
        endpoints.each { endpoint ->
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
        if (!camelContext.isStarted())
            camelContext.start()
        invocation.proceed()
    }
}
