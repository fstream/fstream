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

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
public class PublishRoutes extends RouteBuilder {

  /**
   * @see http://camel.apache.org/direct-vm.html
   */
  public static final String PRODUCER_ENDPOINT = "direct-vm:publish";
  
  /**
   * @see http://camel.apache.org/direct.html
   */
  private static final String PROFILE_ENDPOINT = "direct:profile";

  @Override
  public void configure() throws Exception {
    from(PRODUCER_ENDPOINT)
      .marshal(new CodecDataFormat())
      .to(PROFILE_ENDPOINT) // Delegate to profile specific routes
      .to("metrics:meter:toq"); // Record metrics
  }

  @Component
  @Profile("kafka")
  public static class KafkaRoute extends RouteBuilder {
    
    @Override
    public void configure() throws Exception {
      from(PROFILE_ENDPOINT)
        .setHeader(KafkaConstants.PARTITION_KEY, constant("0")) // Single partition
        .to("{{simulate.publish.uri}}"); // Publish
    }
    
  }
  
  @Component
  @Profile("log")
  public static class NoopRoute extends RouteBuilder {
    
    @Override
    public void configure() throws Exception {
      from(PROFILE_ENDPOINT)
        .to("{{simulate.publish.uri}}"); // Publish
    }
    
  }
  
  @Component
  @Profile({"file", "console"})
  public static class AddNewlineRoute extends RouteBuilder {
    
    @Override
    public void configure() throws Exception {
      from(PROFILE_ENDPOINT)
        .transform().simple("${bodyAs(String)}\n") // Add newline for output
        .to("{{simulate.publish.uri}}"); // Publish
    }
    
  }

}