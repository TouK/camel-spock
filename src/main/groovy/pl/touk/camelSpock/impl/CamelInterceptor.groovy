package pl.touk.camelSpock.impl

import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.apache.camel.component.mock.MockEndpoint
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.InterceptSendToMockEndpointStrategy
import org.apache.camel.impl.SimpleRegistry
import org.apache.camel.spi.Registry
import org.apache.camel.util.ObjectHelper
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FieldInfo
import org.spockframework.runtime.model.SpecInfo
import pl.touk.camelSpock.CamelMock
import pl.touk.camelSpock.annotations.Endpoint
import pl.touk.camelSpock.annotations.RegistryBean

import java.lang.reflect.Method

class CamelInterceptor implements IMethodInterceptor {

    DefaultCamelContext camelContext;

    SpecInfo specInfo;

    Registry registry;

    SimpleRegistry simpleRegistry;

    List<FieldInfo> fields = []

    List<FieldInfo> mockEndpoints = []

    CamelInterceptor(DefaultCamelContext context,SimpleRegistry simpleRegistry, Registry registry, SpecInfo spec) {
        this.simpleRegistry = simpleRegistry
        this.camelContext = context
        this.specInfo = spec
        this.registry = registry
    }

    @Override
    void intercept(IMethodInvocation invocation) throws Throwable {
        Object spec = invocation.target
        fields.each { field ->
            processField(spec, field)
        }
        mockEndpoints.each { endpoint ->
            processMockEndpoint(spec, endpoint)
        }
        customizeContext(spec)
        customizeRegistry(spec)
        if (!camelContext.isStarted())
            camelContext.start()
        invocation.proceed()
    }

    private void processField(Object spec, FieldInfo field) {
        String name = field.getAnnotation(RegistryBean).value()
        if (name == "") name = field.name
        if (simpleRegistry != null) {
            simpleRegistry[name] = field.readValue(spec)
        }
        if (registry != null) {
            field.writeValue(spec, registry.lookupByName(name))
        }
    }

    private void processMockEndpoint(Object spec, FieldInfo endpoint) {
        MockEndpoint mockEndpoint = retrieveMockEndpointFromField(endpoint)

        CamelMock camelMock = endpoint.readValue(spec) as CamelMock
        mockEndpoint.whenAnyExchangeReceived({ Exchange ex ->
            camelMock.receive(ex)
            Object ret = camelMock.receiveBody(ex.in.body)
            if (ret != null) {
                ex.in.body = ret
            }
        } as Processor)
    }

    private MockEndpoint retrieveMockEndpointFromField(FieldInfo endpoint) {
        String name = endpoint.getAnnotation(Endpoint).value()
        if (name == "") name = endpoint.name
        String key = prepareMockKey(name)
        MockEndpoint mockEndpoint = camelContext.getEndpoint(key, MockEndpoint)
        if (name.contains(":")) {
            String pattern = name.contains('?') ? ObjectHelper.before(name, "?") : name
            camelContext.addRegisterEndpointCallback(new InterceptSendToMockEndpointStrategy(pattern+"*", true))

        }
        if (simpleRegistry != null) {
            simpleRegistry[name] = mockEndpoint
        }
        mockEndpoint
    }

    /**
     * @see InterceptSendToMockEndpointStrategy.registerEndpoint
     */
    private String prepareMockKey(String name) {
        String key = "mock:" + name.replaceFirst("://", ":");
        // strip off parameters as well
        if (key.contains("?")) {
            key = ObjectHelper.before(key, "?");
        }
        key
    }

    def customizeRegistry(Object spec) {
        Method method = spec.class.methods.find { it.name == "prepareRegistry" && it.parameterTypes == [SimpleRegistry] }
        method?.invoke(spec, simpleRegistry)
    }

    def customizeContext(Object spec) {
        Method method = spec.class.methods.find { it.name == "prepareContext" && it.parameterTypes == [CamelContext] }
        method?.invoke(spec, camelContext)
    }


}
