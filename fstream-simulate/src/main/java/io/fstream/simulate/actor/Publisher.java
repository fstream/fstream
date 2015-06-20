/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.actor;

import static io.fstream.simulate.routes.ToqRoutes.TOQ_ENDPOINT;
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
  private String endpointUri = TOQ_ENDPOINT;

  @Override
  public String getEndpointUri() {
    return endpointUri;
  }

}
