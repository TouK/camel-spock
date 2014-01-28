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

    }
}

class TestBean {

    def process(String data) {
        "tralaa ${data}"
    }

}

class LogProcessor implements Processor{
    @Override
    void process(Exchange exchange) throws Exception {
        println "IN: ${exchange.in.body}"
        println "OUT: ${exchange.out.body}"
    }
}
