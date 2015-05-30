/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.storm;

import static io.fstream.core.util.ZooKeepers.parseZkPort;
import static io.fstream.core.util.ZooKeepers.parseZkServers;

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
  @Value("${zk.connect}")
  private String zkConnect;

  /**
   * State.
   */
  private LocalCluster cluster;

  @PostConstruct
  public void initialize() {
    log.info("Creating local cluster using external zookeeper(s): {}...", zkConnect);

    val zkServers = parseZkServers(zkConnect);
    val zkPort = parseZkPort(zkConnect);
    this.cluster = LocalClusters.createLocalCluster(zkServers, zkPort);

    logClusterState();
  }

  @Override
  public void execute(@NonNull StormJob job) {
    val totalAvailableSlots = getTotalAvailableSlots();
    if (totalAvailableSlots == 0) {
      log.warn("*** No slots available!!!");
    }

    log.info("Submitting local storm job '{}'  with config {}...", job.getId(), job.getConfig());
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

  private int getTotalAvailableSlots() {
    val summary = cluster.getClusterInfo();

    return getTotalAvailableSlots(summary);
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
