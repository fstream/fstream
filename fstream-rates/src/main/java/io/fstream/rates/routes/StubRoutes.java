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
import io.fstream.rates.handler.RandomRates;
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
public class StubRoutes extends RouteBuilder {

  @Override
  public void configure() throws Exception {
    from("timer://rates?period=100")
      .process(new RandomRates())
      .setHeader(KafkaConstants.PARTITION_KEY, constant("0"))
      .log("${body}")
      .marshal(new CodecDataFormat())
      .to("{{fstream.broker.uri}}");
  }

}