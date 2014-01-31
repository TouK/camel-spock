package pl.touk.camelSpock

import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.apache.camel.builder.RouteBuilder

class TestRoute extends RouteBuilder {

    @Override
    void configure() throws Exception {

        from("direct:ref")
            .to("ref:data");

        from("direct:bean")
            .beanRef("testBean","process");

        from("ref:reference")
               .transform(constant("bla"));

    }
}

class TestBean {

    def process(String data) {
        "tralaa ${data}"
    }

}
