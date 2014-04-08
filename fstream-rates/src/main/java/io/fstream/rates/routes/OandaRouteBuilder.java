/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.rates.routes;

import io.fstream.rates.handler.LogonHandler;
import io.fstream.rates.handler.RatesRegistration;

import org.apache.camel.component.kafka.KafkaConstants;

public class OandaRouteBuilder extends AbstractRouteBuilder {

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
            .setHeader(KafkaConstants.PARTITION_KEY, constant("1"))
            .to("{{fstream.broker.uri}}");
    // @formatter:on
  }

}