/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.esper;

import static java.util.UUID.randomUUID;
import io.fstream.core.model.state.State;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("esper")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EsperJobFactory {

  /**
   * Dependencies.
   */
  @NonNull
  private final EsperProducer producer;

  public EsperJob createJob(State state) {
    val jobId = createJobId("esper-job");

    return new EsperJob(jobId, state, producer);
  }

  private static String createJobId(String prefix) {
    return prefix + "-" + randomUUID().toString();
  }

}
