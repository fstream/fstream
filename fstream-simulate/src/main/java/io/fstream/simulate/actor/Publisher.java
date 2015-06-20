/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.actor;

import io.fstream.simulate.routes.PublishRoutes;
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
  private String endpointUri = PublishRoutes.PUBLISH_ENDPOINT;

  @Override
  public String getEndpointUri() {
    return endpointUri;
  }

}
