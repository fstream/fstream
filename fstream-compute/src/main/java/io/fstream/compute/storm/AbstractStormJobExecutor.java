/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.storm;

import lombok.NonNull;
import lombok.Setter;

/**
 * Convenience template for {@code StormJobExecutor} implementations.
 */
@Setter
public abstract class AbstractStormJobExecutor implements StormJobExecutor {

  protected static void onShutdown(@NonNull Runnable runnable) {
    Runtime.getRuntime().addShutdownHook(new Thread(runnable));
  }

}
