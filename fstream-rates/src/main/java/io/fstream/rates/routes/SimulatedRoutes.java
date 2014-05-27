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
    // Sources
    //
    
    from("timer://ticks1?period=100")
      .process(new RandomTickEventGenerator("EUR/USD", 1.2757f, 1.3990f))
      .to("direct:sink");
    from("timer://ticks2?period=100")
      .process(new RandomTickEventGenerator("EUR/GBP", 0.8081f, 0.8774f))
      .to("direct:sink");
    from("timer://ticks3?period=100")
      .process(new RandomTickEventGenerator("EUR/JPY", 125.0380f, 145.6186f))
      .to("direct:sink");
    from("timer://ticks4?period=100")
      .process(new RandomTickEventGenerator("AUD/JPY", 86.4228f, 99.0119f))
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