/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
// @formatter:off

package io.fstream.simulate.routes;

import io.fstream.simulate.util.CodecDataFormat;

import org.apache.camel.component.kafka.KafkaConstants;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Publication of TOQ.
 */
@Component
@Profile("toq")
public class ToqRoutes extends AbstractRoutes {

  /**
   * @see http://camel.apache.org/direct-vm.html
   */
  public static final String TOQ_ENDPOINT = "direct-vm:toq";
  
  @Override
  public void configure() throws Exception {
    from(TOQ_ENDPOINT)
      .marshal(new CodecDataFormat())
      .choice()
        .when(profilesActive("console", "file"))
          .transform().simple("${bodyAs(String)}\n") // Add newline for correct output
      .end()
      .choice()
        .when(profilesActive("console"))
          .to("stream:out")
      .end()
      .choice()
        .when(profilesActive("file"))
          .to("file:build?fileName=fstream-simulate-toq.json")
      .end()
      .choice()
        .when(profilesActive("log"))
          .to("log:toq?showExchangePattern=false&showBodyType=false")
      .end()
      .choice()
        .when(profilesActive("kafka"))
          .setHeader(KafkaConstants.PARTITION_KEY, constant("part-0")) // Single partition for now
          .to("{{simulate.toq.uri}}")
      .end()
      .to("metrics:meter:toq"); // Record metrics
  }

}