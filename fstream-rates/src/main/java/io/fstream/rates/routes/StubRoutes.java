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

import static org.apache.camel.model.dataformat.JsonLibrary.Jackson;
import io.fstream.core.model.Rate;

import java.math.BigDecimal;

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
        
        int bid = 1;
        int ask = 1;
        
        @Override
        public void process(Exchange exchange) throws Exception {
          val rate = new Rate(new DateTime(), "EUR/USD", new BigDecimal(bid+=1), new BigDecimal(ask+=2));
          
          exchange.getOut().setBody(rate);
        }
        
      })
      .setHeader(KafkaConstants.PARTITION_KEY, constant("0"))
      .log("${body}")
      .marshal().json(Jackson)
      .to("{{fstream.broker.uri}}");
  }

}