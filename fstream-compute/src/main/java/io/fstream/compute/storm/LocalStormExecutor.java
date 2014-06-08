/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.storm;

import javax.annotation.PreDestroy;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import backtype.storm.LocalCluster;

/**
 * Service responsible for launching local topologies.
 */
@Slf4j
@Setter
public class LocalStormExecutor extends AbstractStormExecutor {

  /**
   * State.
   * 
   * @see https://issues.apache.org/jira/browse/STORM-213
   */
  private final LocalCluster cluster = new LocalCluster();

  @Override
  protected void executeJob(StormJob job) {
    log.info("Submitting local topology '{}'...", job.getId());
    cluster.submitTopology(job.getId(), job.getConfig(), job.getTopology());
  }

  @PreDestroy
  public void shutdown() {
    log.info("Shutting down cluster...");
    cluster.shutdown();
    log.info("Shut down cluster.");
  }

}