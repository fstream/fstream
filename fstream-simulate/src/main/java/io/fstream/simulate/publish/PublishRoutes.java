/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
// @formatter:off

package io.fstream.simulate.publish;

import lombok.Setter;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.springframework.stereotype.Component;

@Setter
@Component
public class PublishRoutes extends RouteBuilder {

  /**
   * Sends to a camel context identified by the queue name 'publish'.
   */
  public static final String PRODUCER_ENDPOINT = "direct-vm:publish";
  
  @Override
  public void configure() throws Exception {
    from("direct-vm:publish")
      .setHeader(KafkaConstants.PARTITION_KEY, constant("0"))
      .marshal(new CodecDataFormat())    
      .to("{{simulate.publish.uri}}")
      .to("metrics:meter:toq");
  }

}