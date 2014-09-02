package pl.touk.camelSpock

import pl.touk.camelSpock.annotations.Context
import pl.touk.camelSpock.annotations.Endpoint
import pl.touk.camelSpock.annotations.RegistryBean
import spock.lang.Specification

@Context(ctx = ["testContext.xml"])
class SpringCamelSpec extends Specification{

    @Endpoint("testService")
    CamelMock testService = Mock()

    @RegistryBean("testBean")
    Transformer transformer

    def "Should inject mock endpoints and beans"() {
        given:
            transformer.prefix = prefix
        when:
            sendBody("direct:test",body)
        then:
            1 * testService.receiveBody(prefix+body)
        where:
            prefix | body
            "bla"  | "buhaha"
    }


    static class Transformer {

        String prefix

        def transform(String body) {
            prefix + body
        }

    }

}


