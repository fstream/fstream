/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.storm;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;

import backtype.storm.LocalCluster;

/**
 * {@code StormJobExecutor} implementation responsible for executing local topologies.
 */
@Slf4j
@Setter
public class LocalStormJobExecutor extends AbstractStormJobExecutor {

  /**
   * Configuration.
   */
  @Value("${zk.host}")
  private String zkHost;
  @Value("${zk.port}")
  private long zkPort;

  /**
   * State.
   */
  private LocalCluster cluster;

  @PostConstruct
  public void initialize() {
    log.info("Creating local cluster using external zookeeper: {}:{}...", zkHost, zkPort);
    this.cluster = new LocalCluster(zkHost, zkPort);
  }

  @Override
  public void execute(@NonNull StormJob job) {
    log.info("Submitting local storm job '{}'...", job.getId());
    cluster.submitTopology(job.getId(), job.getConfig(), job.getTopology());
    log.info("Finished submitting local storm job '{}'", job.getId());

    logClusterState();
  }

  @PreDestroy
  public void shutdown() {
    log.info("Shutting down cluster...");
    cluster.shutdown();
    log.info("Finished shutting down cluster");
  }

  private void logClusterState() {
    log.info("Cluster supervisors:");
    for (val supervisor : cluster.getClusterInfo().get_supervisors()) {
      log.info("  {}", supervisor);
    }

    log.info("Cluster topologies:");
    for (val topology : cluster.getClusterInfo().get_topologies()) {
      log.info("  {}", topology);
    }

    log.info("Cluster state:");
    for (val entry : cluster.getState().entrySet()) {
      log.info("  {}", entry);
    }
  }

}
