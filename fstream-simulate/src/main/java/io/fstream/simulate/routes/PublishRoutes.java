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
import lombok.val;

import org.apache.camel.builder.SimpleBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.springframework.stereotype.Component;

/**
 * Publication of TOQ.
 */
@Component
public class PublishRoutes extends AbstractRoutes {

  /**
   * @see http://camel.apache.org/direct-vm.html
   */
  public static final String PUBLISH_ENDPOINT = "direct-vm:publish";
  
  @Override
  public void configure() throws Exception {
    val route = from(PUBLISH_ENDPOINT);
        
    route
      .setHeader("eventType", eventType())
      .marshal(new CodecDataFormat());
    
    if (isProfilesActive("console", "file")) {
      route
        .transform()
        .simple(addNewline()); // Add newline for correct output;
    }
    
    if (isProfilesActive("console")) {
      route
        .to("stream:out");
    }
    if (isProfilesActive("file")) {
      route
        .recipientList(simple("stream:file?fileName={{simulate.file.dir}}/fstream-simulate-${header.eventType}.json"));
    }
    if (isProfilesActive("log")) {
      route
        .recipientList(simple("log:${header.eventType}?showExchangePattern=false&showBodyType=false"));
    }
    if (isProfilesActive("kafka")) {
      route
        .setHeader(KafkaConstants.PARTITION_KEY, kafkaPartition()) // Single partition for now
        .recipientList(simple("{{simulate.kafka.uri}}&topic=${header.eventType}"));
    }
    
    route
      .recipientList(simple("metrics:${header.eventType}"));
  }

  private SimpleBuilder eventType() {
    return simple("${body.type.toString().toLowerCase()}s");
  }
   
}