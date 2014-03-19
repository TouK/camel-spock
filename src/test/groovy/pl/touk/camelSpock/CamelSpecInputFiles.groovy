package pl.touk.camelSpock

import pl.touk.camelSpock.annotations.Context
import pl.touk.camelSpock.annotations.Endpoint
import pl.touk.camelSpock.annotations.RegistryBean
import pl.touk.camelSpock.utils.CamelSpockUtils
import spock.lang.Specification

@Context(routeBuilders = [TestRoute])
@Mixin(CamelSpockUtils)
class CamelSpecInputFiles extends Specification{


    @RegistryBean("testBean")
    TestBean testBean = Mock()

    @Endpoint("data")
    CamelMock camelMock = Mock()

    def "Should check received at mock"() {
        when:
            sendBody("direct:ref", fromClassPath("testInput.xml"))
        then:
            1 * camelMock.receive({ it.in.body == "buhaha"})
    }
}
