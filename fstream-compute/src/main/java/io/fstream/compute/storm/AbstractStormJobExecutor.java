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
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import backtype.storm.generated.ClusterSummary;

/**
 * Convenience template for {@code StormJobExecutor} implementations.
 */
@Slf4j
@Setter
public abstract class AbstractStormJobExecutor implements StormJobExecutor {

  protected int getTotalAvailableSlots(@NonNull ClusterSummary summary) {
    int totalSlots = 0;
    int totalUsedSlots = 0;
    for (val supervisor : summary.get_supervisors()) {
      totalSlots += supervisor.get_num_workers();
      totalUsedSlots += supervisor.get_num_used_workers();
    }

    val totalAvailableSlots = totalSlots - totalUsedSlots;
    log.info("Storm supervisor state -  total slots: {}, total used slots: {}, total available slots: {}", totalSlots,
        totalUsedSlots, totalAvailableSlots);

    return totalAvailableSlots;
  }

  protected static void onShutdown(@NonNull Runnable runnable) {
    Runtime.getRuntime().addShutdownHook(new Thread(runnable));
  }

}
