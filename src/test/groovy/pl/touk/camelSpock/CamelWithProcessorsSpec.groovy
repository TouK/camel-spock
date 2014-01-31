package pl.touk.camelSpock

import org.apache.camel.Exchange
import org.apache.camel.ExchangePattern
import org.apache.camel.impl.SimpleRegistry
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

    def prepareRegistry(SimpleRegistry registry) {
        registry["reference"] = getEndpoint("direct:reference")
    }


    def "InOnly should leave message on in endpoint"() {
        when:
            Exchange result = inOut("direct:ref",input)
        then:
            result.in.body == input
            result.out.body == output
            1 * camelMock.receive({ it.in.body == input})
        where:
            input      | output
            "tralala"  | null
    }

    def "InOnly should leave message on in endpoint -- more sugar"() {
        when:
            Exchange result = inOnly("direct:ref",input)
        then:
            result.in.body == input
            result.out.body == output
            1 * camelMock.receive({ it.in.body == input})
        where:
            input      | output
            "tralala"  | null
    }

    def "InOnly is the same as sendBody"() {
        when:
            String result = sendBody("direct:ref", input)
        then:
            result == output
            1 * camelMock.receive({ it.in.body == input})
        where:
            input     | output
            "tralala" | null
    }

    def "InOut should leave message on out endpoint"() {
        given:
            testBean.process(input) >> output
        when:
            Exchange result = inOut("direct:bean", input)
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

    def "InOut is the same as requestBody -- more sugar"() {
        given:
            testBean.process(input) >> output
        when:
            Exchange result = inOut("direct:bean",input)
        then:
            result.in.body == input
            result.out.body == output
        where:
            input      | output
            "tralala"  | "out"
    }
}
