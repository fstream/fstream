/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
// @formatter:off

package io.fstream.feed.routes;

import static org.apache.camel.LoggingLevel.DEBUG;
import io.fstream.core.model.event.Quote;
import io.fstream.feed.util.CodecDataFormat;

import org.apache.camel.component.kafka.KafkaConstants;
import org.springframework.stereotype.Component;

/**
 * Route definitions for FIX handling.
 */
@Component
public class FixRoutes extends AbstractFixRoutes {
  
  @Override
  public void configure() throws Exception {
    onException(Throwable.class)
      .log("${exception}")
      .handled(true);
    
    from("{{feed.uri}}")
      .to("metrics:meter:feed") // Record metrics 
      .choice()
        .when(logon())
          .to("bean:logonHandler")
          
        .when(sessionLogon())
          .to("bean:feedRegistration")
          .to("{{feed.uri}}")
          
        .when(marketDataSnapshotFullRefresh())
          .convertBodyTo(Quote.class)
          .log(DEBUG, "${body}")
          .marshal(new CodecDataFormat())
          .setHeader(KafkaConstants.PARTITION_KEY, constant("0")) // Required
          .to("{{fstream.broker.uri}}")  // Note: http://grokbase.com/t/kafka/users/138vqq1x07/getting-leadernotavailableexception-in-console-producer-after-increasing-partitions-from-4-to-16
    
        .when(marketDataRequestReject())
          .to("bean:fixMessageLogger")
          .throwException(new RuntimeException("marketDataRequestReject"));
    
    // Debugging
    from("stub:{{feed.uri}}")
      .to("bean:fixMessageLogger");
  }
  
}