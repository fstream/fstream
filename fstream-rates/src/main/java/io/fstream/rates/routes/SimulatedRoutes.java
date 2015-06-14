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

import io.fstream.rates.camel.CodecDataFormat;
import io.fstream.rates.handler.RandomTickEventGenerator;
import lombok.Setter;
import lombok.val;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Stub route definitions for OANDA FIX handling.
 */
@Profile("simulation")
@Setter
@Component
public class SimulatedRoutes extends RouteBuilder {

  @Override
  public void configure() throws Exception {
    
    //
    // Sources (6 Majors)
    //
    
    val period = 5000L;
    
    from("timer://tick-event1?period=" + period * 1)
      .process(new RandomTickEventGenerator("EUR/USD", 1.2757f, 1.3990f))
      .to("direct:sink")
      .to("metrics:meter:eurusd"); 
    from("timer://tick-event2?period=" + period * 2)
      .process(new RandomTickEventGenerator("USD/JPY", 93.8675f, 105.4415f))
      .to("direct:sink");
    from("timer://tick-event3?period=" + period * 3)
      .process(new RandomTickEventGenerator("GBP/USD", 1.4817f, 1.6997f))
      .to("direct:sink");
    from("timer://tick-event4?period=" + period * 4)
      .process(new RandomTickEventGenerator("AUD/USD", 0.8661f, 0.9791f))
      .to("direct:sink");
    from("timer://tick-event5?period=" + period * 5)
      .process(new RandomTickEventGenerator("USD/CHF", 0.8701f, 0.9792f))
      .to("direct:sink");
    from("timer://tick-event6?period=" + period * 6)
      .process(new RandomTickEventGenerator("USD/CAD", 1.0138f, 1.1279f))
      .to("direct:sink");
    from("timer://tick-event6?period=" + period * 6)
      .process(new RandomTickEventGenerator("AUD/NZD", 1.0138f, 1.1279f))
      .to("direct:sink");
    from("timer://tick-event6?period=" + period * 6)
      .process(new RandomTickEventGenerator("NZD/USD", 1.0138f, 1.1279f))
      .to("direct:sink");    
    
    //
    // Sink
    //
    
    from("direct:sink")
      .setHeader(KafkaConstants.PARTITION_KEY, constant("0"))
      .log("${body}")
      .marshal(new CodecDataFormat())
      .to("{{fstream.broker.uri}}");
    
  }

}