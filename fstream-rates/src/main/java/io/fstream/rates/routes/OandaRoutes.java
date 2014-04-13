/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.rates.routes;

import io.fstream.core.model.Rate;
import io.fstream.rates.handler.FixMessageLogger;
import io.fstream.rates.handler.LogonHandler;
import io.fstream.rates.handler.RatesRegistration;

import org.apache.camel.component.kafka.KafkaConstants;

/**
 * Route definitions for OANDA FIX handling.
 */
public class OandaRoutes extends AbstractFixRoutes {

  @Override
  public void configure() throws Exception {
    // @formatter:off
    from("{{oanda.rates.uri}}")
      .choice()
        .when(logon())
          .bean(LogonHandler.class)
          
        .when(sessionLogon())
          .bean(RatesRegistration.class)
          .to("{{oanda.rates.uri}}")
          
        .when(marketDataSnapshotFullRefresh())
          .convertBodyTo(Rate.class)
          .log("${body}")
          .setHeader(KafkaConstants.PARTITION_KEY, constant("0"))
          .to("{{fstream.broker.uri}}");  // Note: http://grokbase.com/t/kafka/users/138vqq1x07/getting-leadernotavailableexception-in-console-producer-after-increasing-partitions-from-4-to-16
    
    // For debugging
    from("stub:direct:fix")
      .bean(FixMessageLogger.class);
    // @formatter:on
  }
}