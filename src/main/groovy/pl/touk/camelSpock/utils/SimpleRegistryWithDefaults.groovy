package pl.touk.camelSpock.utils

import org.apache.camel.CamelContext
import org.apache.camel.Endpoint
import org.apache.camel.impl.SimpleRegistry

class SimpleRegistryWithDefaults extends SimpleRegistry {

    CamelContext camelContext
    boolean resolveRefEndpoints

    @Override
    def <T> T lookupByNameAndType(String name, Class<T> type) {
        T obj = super.lookupByNameAndType(name, type)
        if (!obj && resolveRefEndpoints && Endpoint.isAssignableFrom(type)) {
            obj = camelContext.getEndpoint("direct:"+name) as T
        }
        obj
    }
}
