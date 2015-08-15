/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.actor.publisher;

import io.fstream.core.model.event.Event;
import io.fstream.simulate.output.Output;

import java.util.List;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import akka.actor.UntypedActor;

@RequiredArgsConstructor
public class OutputPublisher extends UntypedActor {

  @NonNull
  private final List<Output> outputs;

  @Override
  public void onReceive(Object message) throws Exception {
    val event = (Event) message;
    for (val output : outputs) {
      output.write(event);
    }
  }

}
