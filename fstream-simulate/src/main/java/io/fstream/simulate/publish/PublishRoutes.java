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

import static java.util.Collections.disjoint;

import java.util.List;

import lombok.NonNull;
import lombok.val;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.ValueBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

@Component
public class PublishRoutes extends RouteBuilder {

  /**
   * @see http://camel.apache.org/direct-vm.html
   */
  public static final String PUBLISH_ENDPOINT = "direct-vm:publish";
  
  /**
   * Configuration.
   */
  @Value("#{environment.getActiveProfiles()}")
  List<String> activeProfiles;
  
  @Override
  public void configure() throws Exception {
    from(PUBLISH_ENDPOINT)
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
          .to("file:build?fileName=fstream-simulation.json")
      .end()
      .choice()
        .when(profilesActive("log"))
          .to("log:toq?showExchangePattern=false&showBodyType=false")
      .end()
      .choice()
        .when(profilesActive("kafka"))
          .setHeader(KafkaConstants.PARTITION_KEY, constant("part-0")) // Single partition for now
          .to("{{camel.kafka.uri}}")
      .end()
      .to("metrics:meter:toq"); // Record metrics
  }
  
  @NonNull
  private ValueBuilder profilesActive(String... profiles) {
    val active = !disjoint(activeProfiles, ImmutableList.copyOf(profiles));
   
    return constant(active);
  }

}