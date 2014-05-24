/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
// @formatter:off

package io.fstream.rates.routes;

import io.fstream.core.model.event.TickEvent;
import io.fstream.rates.camel.CodecDataFormat;
import lombok.Setter;
import lombok.val;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

/**
 * Stub route definitions for OANDA FIX handling.
 */
@Setter
@Component
public class StubRoutes extends RouteBuilder {

  @Override
  public void configure() throws Exception {
    from("timer://rates?period=1000")
      .process(new Processor() {
        
        float bid = 1.0f;
        float ask = 1.0f;
        
        @Override
        public void process(Exchange exchange) throws Exception {
          ask = (float)Math.random()+1;
          bid = ask - 0.001f;
          val event = new TickEvent(new DateTime(), "EUR/USD", ask, bid);
          
          exchange.getOut().setBody(event);
        }
        
      })
      .setHeader(KafkaConstants.PARTITION_KEY, constant("0"))
      .log("${body}")
      .marshal(new CodecDataFormat())
      .to("{{fstream.broker.uri}}");
  }

}