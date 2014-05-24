/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.service;

import io.fstream.compute.config.StormProperties;

import javax.annotation.PostConstruct;

import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.thrift7.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.generated.KillOptions;
import backtype.storm.generated.NotAliveException;
import backtype.storm.generated.StormTopology;
import backtype.storm.utils.NimbusClient;
import backtype.storm.utils.Utils;

/**
 * Service responsible for launching topologies.
 */
@Slf4j
@Service
@Setter
public class ComputeService {

  /**
   * Constants.
   */
  private static final String TOPOLOGY_NAME = "compute-topology";

  /**
   * Configuration.
   */
  @Autowired
  private StormProperties stormProperties;

  /**
   * Dependencies.
   */
  @Autowired
  private StormService stormService;

  @PostConstruct
  public void execute() throws Exception {
    // Setup
    val topology = stormService.createTopology();
    val config = stormService.createConfig();

    if (stormProperties.isLocal()) {
      executeLocal(topology, config);
    } else {
      executeCluster(topology, config);
    }
  }

  private void executeLocal(StormTopology topology, Config config) {
    log.info("Submitting local topology '{}'...", TOPOLOGY_NAME);
    val cluster = new LocalCluster();
    cluster.submitTopology(TOPOLOGY_NAME, config, topology);

    onShutdown(new Runnable() {

      @Override
      public void run() {
        log.info("Shutting down cluster...");
        cluster.shutdown();
        log.info("Shut down cluster.");
      }

    });
  }

  private void executeCluster(StormTopology topology, Config config)
      throws AlreadyAliveException, InvalidTopologyException {
    log.info("Submitting cluster topology '{}'...", TOPOLOGY_NAME);
    StormSubmitter.submitTopology(TOPOLOGY_NAME, config, topology);

    onShutdown(new Runnable() {

      @Override
      @SneakyThrows
      public void run() {
        killTopology(TOPOLOGY_NAME);
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
