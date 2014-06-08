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
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.thrift7.TException;

import backtype.storm.StormSubmitter;
import backtype.storm.generated.KillOptions;
import backtype.storm.generated.NotAliveException;
import backtype.storm.utils.NimbusClient;
import backtype.storm.utils.Utils;

/**
 * Service responsible for launching distributed topologies.
 */
@Slf4j
@Setter
public class DistributedStormExecutor extends AbstractStormExecutor {

  @Override
  @SneakyThrows
  protected void executeJob(final StormJob job) {
    log.info("Submitting cluster topology '{}'...", job.getId());
    StormSubmitter.submitTopology(job.getId(), job.getConfig(), job.getTopology());

    onShutdown(new Runnable() {

      @Override
      @SneakyThrows
      public void run() {
        log.info("Killing topology...");
        killTopology(job.getId());
        log.info("Killed topology");
      }

    });
  }

  private static void killTopology(String name) throws NotAliveException, TException {
    val client = NimbusClient.getConfiguredClient(Utils.readStormConfig()).getClient();
    val killOpts = new KillOptions();

    log.info("Killing topology '{}'", name);
    client.killTopologyWithOpts(name, killOpts);
    log.info("Killed topology '{}'.", name);
  }

}
