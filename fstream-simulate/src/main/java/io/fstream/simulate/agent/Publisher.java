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
import lombok.NonNull;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import akka.camel.javaapi.UntypedProducerActor;

@Lazy
@Component
@NoArgsConstructor
@AllArgsConstructor
public class Publisher extends UntypedProducerActor {

  /**
   * Sends to a camel context identified by the queue name 'publish'.
   */
  private static final String DEFAULT_PRODUCER_ENDPOINT = "direct-vm:publish";

  /**
   * The endpoint to publish to.
   */
  @NonNull
  private String endpointUrl = DEFAULT_PRODUCER_ENDPOINT;

  @Override
  public String getEndpointUri() {
    return endpointUrl;
  }

}
