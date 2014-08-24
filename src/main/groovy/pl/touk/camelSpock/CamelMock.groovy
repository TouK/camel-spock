package pl.touk.camelSpock

import org.apache.camel.Exchange

/**
 * This interface is used to wrap endpoints injected with @Endpoint annotation
 */
interface CamelMock {

    void receive(Exchange exchange)

    Object receiveBody(Object body)

}
