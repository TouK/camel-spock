package pl.touk.camelSpock

import org.apache.camel.Exchange
import org.apache.camel.Processor

class ExchangeComposer implements Processor{
    String inBody
    String outBody

    @Override
    void process(Exchange exchange) throws Exception {
        exchange.in.body = inBody
        exchange.out.body = outBody
    }
}
