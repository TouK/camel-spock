package pl.touk.camelSpock

import org.apache.camel.Exchange
import org.apache.camel.ExchangePattern
import pl.touk.camelSpock.annotations.Context
import pl.touk.camelSpock.annotations.Endpoint
import pl.touk.camelSpock.annotations.RegistryBean
import spock.lang.Specification

@Context(routeBuilders = [TestRoute])
class CamelWithProcessorsSpec extends Specification{


    @RegistryBean("testBean")
    TestBean testBean = Mock()

    @Endpoint("data")
    CamelMock camelMock = Mock()

    def "InOnly should leave message on in endpoint"() {
        given:
            testBean.process(input) >> output
        when:
            Exchange result = send("direct:bean",ExchangePattern.InOnly, new ExchangeComposer(inBody: input, outBody: null))
        then:
            result.in.body == output
            result.out.body == null
        where:
            input      | output
            "tralala"  | "out"
    }

    def "InOnly is the same as sendBody"() {
        given:
            testBean.process(input) >> output
        when:
            String result = sendBody("direct:bean", input)
        then:
            result == output
        where:
            input     | output
            "tralala" | "out"
    }

    def "InOut should leave message on out endpoint"() {
        given:
            testBean.process(input) >> output
        when:
            Exchange result = send("direct:bean",ExchangePattern.InOut, new ExchangeComposer(inBody: input, outBody: null))
        then:
            result.in.body == input
            result.out.body == output
        where:
            input      | output
            "tralala"  | "out"
    }

    def "InOut is the same as requestBody"() {
        given:
            testBean.process(input) >> output
        when:
            String result = requestBody("direct:bean", input)
        then:
            result == output
        where:
            input     | output
            "tralala" | "out"
    }
}