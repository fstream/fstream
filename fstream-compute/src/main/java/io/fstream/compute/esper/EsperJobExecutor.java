/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.esper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("esper")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EsperJobExecutor {

  /**
   * Dependencies.
   */
  @NonNull
  private final EsperKafkaConsumer consumer;
  private final ExecutorService executor = Executors.newFixedThreadPool(1);

  /**
   * State.
   */
  private Future<?> future;

  @SneakyThrows
  synchronized public void execute(EsperJob job) {
    if (future != null) {
      log.info("Canceling job {}", job.getJobId());
      future.cancel(true);
    }

    log.info("Submitting job {}", job.getJobId());
    this.future = executor.submit(() -> run(job));
  }

  @SneakyThrows
  private void run(EsperJob job) {
    log.info("Running job {}", job.getJobId());
    val queue = consumer.getQueue();
    while (true) {
      if (Thread.currentThread().isInterrupted()) {
        job.stop();
        return;
      }

      val event = queue.take();
      job.execute(event);
    }
  }

}
