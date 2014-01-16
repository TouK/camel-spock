package pl.touk.camelSpock

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
