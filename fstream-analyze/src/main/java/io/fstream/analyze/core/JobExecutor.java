/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.analyze.core;

import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.AbstractExecutionThreadService;

/**
 * Executes {@link Job}s against a shared {@link JavaStreamingContext} with a provided {@link JobContext}.
 * <p>
 * Runs asynchronously in its own thread.
 */
@Slf4j
@Component
public class JobExecutor extends AbstractExecutionThreadService {

  /**
   * Dependencies.
   */
  @Autowired
  private JavaStreamingContext streamingContext;
  @Autowired
  private List<Job> jobs;

  @PostConstruct
  public void init() throws Exception {
    log.info("Initializing analytics job...");
    startAsync();
    log.info("Finished initializing analytics job");
  }

  @Override
  protected void run() throws IOException {
    registerJobs();
    startJobs();
  }

  private void registerJobs() {
    log.info("Registering {} jobs...", jobs.size());
    for (val job : jobs) {
      job.register();
    }
  }

  private void startJobs() {
    log.info("Starting jobs...");
    streamingContext.start();

    log.info("Awaiting shutdown...");
    streamingContext.awaitTermination();
  }

}
