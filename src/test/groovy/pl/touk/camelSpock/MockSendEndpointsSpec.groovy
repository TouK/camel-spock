package pl.touk.camelSpock

import pl.touk.camelSpock.annotations.Context
import pl.touk.camelSpock.annotations.Endpoint
import spock.lang.Specification

@Context(routeBuilders = [SendTestRoute])
class MockSendEndpointsSpec extends Specification {

    @Endpoint("http://i.dont.exist.pl")
    CamelMock camelMock = Mock()

    def shouldMockSendToEndpoints() {
        when:
            sendBody("direct:testSend", input)
        then:
            1 * camelMock.receiveBody(input)
        where:
            input << ["testInput"]

    }


}
