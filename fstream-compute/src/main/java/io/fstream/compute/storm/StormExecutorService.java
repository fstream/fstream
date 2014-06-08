/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.storm;

import io.fstream.compute.config.KafkaProperties;
import io.fstream.compute.config.StormProperties;
import io.fstream.core.model.state.State;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.thrift7.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.generated.KillOptions;
import backtype.storm.generated.NotAliveException;
import backtype.storm.utils.NimbusClient;
import backtype.storm.utils.Utils;

/**
 * Service responsible for launching topologies.
 */
@Slf4j
@Service
@Setter
public class StormExecutorService {

  /**
   * Configuration.
   */
  @Value("${zk.connect}")
  private String zkConnect;
  @Autowired
  private StormProperties stormProperties;
  @Autowired
  private KafkaProperties kafkaProperties;

  public void execute(State state) throws Exception {
    // Setup
    val job = new StormJob(zkConnect, kafkaProperties, state);

    if (stormProperties.isLocal()) {
      executeLocal(job);
    } else {
      executeCluster(job);
    }
  }

  private void executeLocal(StormJob job) {
    log.info("Submitting local topology '{}'...", job.getName());

    // https://issues.apache.org/jira/browse/STORM-213
    val cluster = new LocalCluster();
    cluster.submitTopology(job.getName(), job.getConfig(), job.getTopology());

    onShutdown(new Runnable() {

      @Override
      public void run() {
        log.info("Shutting down cluster...");
        cluster.shutdown();
        log.info("Shut down cluster.");
      }

    });
  }

  private void executeCluster(final StormJob job)
      throws AlreadyAliveException, InvalidTopologyException {
    log.info("Submitting cluster topology '{}'...", job.getName());
    StormSubmitter.submitTopology(job.getName(), job.getConfig(), job.getTopology());

    onShutdown(new Runnable() {

      @Override
      @SneakyThrows
      public void run() {
        log.info("Killing topology...");
        killTopology(job.getName());
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

  private static void onShutdown(Runnable runnable) {
    Runtime.getRuntime().addShutdownHook(new Thread(runnable));
  }

}
