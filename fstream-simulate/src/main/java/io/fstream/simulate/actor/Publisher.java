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
import io.fstream.simulate.util.SingletonActor;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import akka.camel.javaapi.UntypedProducerActor;

@SingletonActor
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
