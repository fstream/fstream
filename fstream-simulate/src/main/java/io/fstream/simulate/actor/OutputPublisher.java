/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.actor;

import io.fstream.simulate.output.FileOutput;
import akka.actor.UntypedActor;

public class OutputPublisher extends UntypedActor {

  private final FileOutput output = new FileOutput();

  @Override
  public void onReceive(Object message) throws Exception {
    output.write(message);
  }

}
