/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.agent;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import akka.camel.javaapi.UntypedProducerActor;

@Lazy
@Component
@NoArgsConstructor
@AllArgsConstructor
public class Publisher extends UntypedProducerActor {

  private String endpointUrl = "direct-vm:publish";

  @Override
  public String getEndpointUri() {
    return endpointUrl;
  }

}
