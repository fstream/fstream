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

import org.apache.camel.component.kafka.KafkaConstants;
import org.springframework.stereotype.Component;

/**
 * Route definitions for OANDA FIX handling.
 */
@Component
public class OandaRoutes extends AbstractFixRoutes {
  
  @Override
  public void configure() throws Exception {
    from("{{oanda.rates.uri}}")
      .choice()
        .when(logon())
          .to("bean:logonHandler")
          
        .when(sessionLogon())
          .to("bean:ratesRegistration")
          .to("{{oanda.rates.uri}}")
          
        .when(marketDataSnapshotFullRefresh())
          .convertBodyTo(Rate.class)
          .log("${body}")
          .marshal().json(Jackson)
          .setHeader(KafkaConstants.PARTITION_KEY, constant("0"))
          .to("{{fstream.broker.uri}}");  // Note: http://grokbase.com/t/kafka/users/138vqq1x07/getting-leadernotavailableexception-in-console-producer-after-increasing-partitions-from-4-to-16
    
    // For debugging
    from("stub:{{oanda.rates.uri}}")
      .to("bean:fixMessageLogger");
  }
  
}