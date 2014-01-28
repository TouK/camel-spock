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
            String result = requestBody("direct:bean",input)
        then:
            result == output
        where:
            input      | output
            "tralala"  | "out"

    }

    def "Should check received at mock"() {
        when:
            sendBody("direct:ref","buhaha")
        then:
            1 * camelMock.receive({ it.in.body == "buhaha"})
    }


    def "Should reply to mock"() {
        given:
            camelMock.receiveBody("hop") >> "tralaa"
        when:
            String result = requestBody("direct:ref","hop")
        then:
            result == "tralaa"
    }

    def "Should request XML and verify GPath"() {
        given:
            camelMock.receiveBody("hop") >> "<a><b>tralaa</b></a>"
        when:
            def result = requestXml("direct:ref","hop")
        then:
            result.b.text() == "tralaa"
    }

    def "Should verify GPath at mock"() {
        when:
            sendBody("direct:ref","<a><b>buhaha</b></a>")
        then:
            1 * camelMock.receive({ it.in.xml.b.text() == "buhaha"})
    }





}
