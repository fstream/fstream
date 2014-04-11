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
          .setHeader(KafkaConstants.PARTITION_KEY, constant("1"))
          .multicast()
            .to("seda:broker", "seda:analytics");
    
    // Send to broker
    from("seda:broker")
      .log("${body}")
      .to("{{fstream.broker.uri}}");
    
    // Send to process 
    from("seda:analytics")
      .to("esper://rates");
    
    // See http://esper.codehaus.org/esper-4.11.0/doc/reference/en-US/html_single/index.html#epl-intro
    from("esper://rates?eql=" + 
        "SELECT " +
        "  cast(ask, float) / cast(prior(1, ask), float) AS askPercentChange " + 
        "FROM " + 
        "  " + Rate.class.getName() + "") 
        .log("output: '${body.properties}'");
    
    // For debugging
    from("stub:direct:fix")
      .bean(FixMessageLogger.class);
    // @formatter:on
  }

}