package pl.touk.camelSpock

import org.apache.camel.Exchange

interface CamelMock {

    void receive(Exchange exchange)

    Object receiveBody(Object body)

}
