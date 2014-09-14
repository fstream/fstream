/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.storm;

import lombok.Setter;

@Setter
public abstract class AbstractStormExecutor implements StormExecutor {

  @Override
  public abstract void execute(StormJob job);

  protected static void onShutdown(Runnable runnable) {
    Runtime.getRuntime().addShutdownHook(new Thread(runnable));
  }

}
