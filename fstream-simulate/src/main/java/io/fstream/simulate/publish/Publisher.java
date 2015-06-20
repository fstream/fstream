/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.publish;

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
   * The endpoint to publish to.
   */
  @NonNull
  private String endpointUrl = PublishRoutes.PRODUCER_ENDPOINT;

  @Override
  public String getEndpointUri() {
    return endpointUrl;
  }

}
