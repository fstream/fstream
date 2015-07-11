/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.actor;

import static io.fstream.simulate.routes.PublishRoutes.PUBLISH_ENDPOINT;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import akka.camel.javaapi.UntypedProducerActor;

@NoArgsConstructor
@AllArgsConstructor
public class Publisher extends UntypedProducerActor {

  /**
   * The endpoint to publish to.
   */
  @NonNull
  private String endpointUri = PUBLISH_ENDPOINT;

  @Override
  public String getEndpointUri() {
    return endpointUri;
  }

  @Override
  public Object onTransformOutgoingMessage(Object message) {
    // Return a new object or modify the supplied object as needed before it gets dispatched
    return message;
  }

}
