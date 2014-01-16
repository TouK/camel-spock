package pl.touk.camelSpock

import pl.touk.camelSpock.annotations.Context
import pl.touk.camelSpock.annotations.Endpoint
import pl.touk.camelSpock.annotations.RegistryBean
import spock.lang.Specification

@Context(routeBuilders = [TestRoute])
class CamelSpec extends Specification{


    @RegistryBean("testBean")
    TestBean testBean = Mock()

    @Endpoint("data")
    CamelMock camelMock = Mock()

    def "Should invoke bean ref"() {
        given:
            testBean.process(input) >> output
        when:
            String result = request("direct:bean",input)
        then:
            result == output
        where:
            input      | output
            "tralala"  | "out"

    }

    def "Should check received at mock"() {
        when:
            send("direct:ref","buhaha")
        then:
            1 * camelMock.receive({ it.in.body == "buhaha"})
    }


    def "Should reply to mock"() {
        given:
            camelMock.receiveBody("hop") >> "tralaa"
        when:
            String result = request("direct:ref","hop")
        then:
            result == "tralaa"
    }


}
