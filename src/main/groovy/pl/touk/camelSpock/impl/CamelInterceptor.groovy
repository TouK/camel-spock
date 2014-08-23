package pl.touk.camelSpock.impl

import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.apache.camel.component.mock.MockEndpoint
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.SimpleRegistry
import org.apache.camel.spi.Registry
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FieldInfo
import org.spockframework.runtime.model.SpecInfo
import org.spockframework.runtime.model.SpecInfo
import pl.touk.camelSpock.CamelMock
import pl.touk.camelSpock.annotations.Endpoint
import pl.touk.camelSpock.annotations.RegistryBean

import java.lang.reflect.Method

class CamelInterceptor implements IMethodInterceptor{

    DefaultCamelContext camelContext;

    SpecInfo specInfo;

    CamelInterceptor(DefaultCamelContext context,SimpleRegistry simpleRegistry, Registry registry, SpecInfo spec) {
        this.simpleRegistry = simpleRegistry
        this.camelContext = context
        this.specInfo = spec
        this.registry = registry
    }

    Registry registry;

    SimpleRegistry simpleRegistry;

    List<FieldInfo> fields = []

    List<FieldInfo> mockEndpoints = []

    @Override
    void intercept(IMethodInvocation invocation) throws Throwable {
        Object spec = invocation.target
        fields.each { field ->
            String name = field.getAnnotation(RegistryBean).value()
            if (name == "") name = field.name
            if (simpleRegistry != null) {
                simpleRegistry[name] = field.readValue(spec)
            }
            if (registry != null) {
                field.writeValue(spec, registry.lookup(name))
            }
        }
        mockEndpoints.each { endpoint ->
            String name = endpoint.getAnnotation(Endpoint).value()
            if (name == "") name = endpoint.name
            MockEndpoint mockEndpoint
            if (simpleRegistry) {
                mockEndpoint = camelContext.getEndpoint("mock:${name}",MockEndpoint)
                simpleRegistry[name] = mockEndpoint
            } else {
                mockEndpoint = camelContext.getEndpoint(name, MockEndpoint)
            }

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
        method?.invoke(spec, simpleRegistry)
    }

    def customizeContext(Object spec) {
        Method method = spec.class.methods.find { it.name == "prepareContext" && it.parameterTypes == [CamelContext] }
        method?.invoke(spec, simpleRegistry)
    }


}
