package pl.touk.camelSpock

import org.apache.camel.builder.RouteBuilder

class SendTestRoute extends RouteBuilder {

    @Override
    void configure() throws Exception {
        from("direct:testSend")
            .to("http://i.dont.exist.pl?emptyParam=true")
    }
}
