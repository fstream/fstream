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
import io.fstream.rates.util.CodecDataFormat;

import org.apache.camel.component.kafka.KafkaConstants;
import org.springframework.stereotype.Component;

/**
 * Route definitions for FIX handling.
 */
@Component
public class RatesRoutes extends AbstractFixRoutes {
  
  @Override
  public void configure() throws Exception {
    onException(Throwable.class)
      .log("${exception}")
      .handled(true);
    
    from("{{rates.uri}}")
      .choice()
        .when(logon())
          .to("bean:logonHandler")
          
        .when(sessionLogon())
          .to("bean:ratesRegistration")
          .to("{{rates.uri}}")
          
        .when(marketDataSnapshotFullRefresh())
          .convertBodyTo(TickEvent.class)
          .log("${body}")
          .marshal(new CodecDataFormat())
          .setHeader(KafkaConstants.PARTITION_KEY, constant("0"))
          .to("{{fstream.broker.uri}}")  // Note: http://grokbase.com/t/kafka/users/138vqq1x07/getting-leadernotavailableexception-in-console-producer-after-increasing-partitions-from-4-to-16
    
        .when(marketDataRequestReject())
          .to("bean:fixMessageLogger")
          .throwException(new RuntimeException("marketDataRequestReject"));
    
    // For debugging
    from("stub:{{rates.uri}}")
      .to("bean:fixMessageLogger");
  }
  
}