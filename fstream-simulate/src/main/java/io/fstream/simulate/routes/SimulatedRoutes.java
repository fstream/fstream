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

import io.fstream.simulate.publisher.Publisher;
import lombok.Setter;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Setter
@Component
public class SimulatedRoutes extends RouteBuilder {

  /**
   * Dependencies.
   */
  @Autowired 
  private Publisher publisher;
  
  @Override
  public void configure() throws Exception {
    from("direct-vm:publish")
      .bean(publisher)
      .to("metrics:meter:events");
  }

}