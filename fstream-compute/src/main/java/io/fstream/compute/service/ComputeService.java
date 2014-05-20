/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.service;

import io.fstream.compute.config.EsperProperties;
import io.fstream.compute.config.StormProperties;
import io.fstream.compute.factory.StormFactory;

import javax.annotation.PostConstruct;

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
import backtype.storm.generated.KillOptions;
import backtype.storm.generated.NotAliveException;
import backtype.storm.utils.NimbusClient;
import backtype.storm.utils.Utils;

@Slf4j
@Service
@Setter
public class ComputeService {

  /**
   * Contants.
   */
  private static final String TOPOLOGY_NAME = "compute-topology";

  /**
   * Configuration.
   */
  @Value("${zk.connect}")
  private String zkConnect;
  @Autowired
  private StormProperties stormProperities;
  @Autowired
  private EsperProperties esperProperties;

  @PostConstruct
  public void execute() throws Exception {
    // Setup
    val local = stormProperities.isLocal();
    val topology = StormFactory.newStormTopology(zkConnect);
    val config = StormFactory.newStormConfig(
        local,
        stormProperities.getProperties(),
        esperProperties.getEpl());

    if (local) {
      log.info("Submitting local topology '{}'...", TOPOLOGY_NAME);
      val cluster = new LocalCluster();
      cluster.submitTopology(TOPOLOGY_NAME, config, topology);

      Runtime.getRuntime().addShutdownHook(new Thread() {

        @Override
        public void run() {
          cluster.shutdown();
          log.info("Shut down topoloy '{}'", TOPOLOGY_NAME);
        }

      });
    } else {
      log.info("Submitting cluster topology '{}'...", TOPOLOGY_NAME);
      StormSubmitter.submitTopology(TOPOLOGY_NAME, config, topology);

      Runtime.getRuntime().addShutdownHook(new Thread() {

        @Override
        @SneakyThrows
        public void run() {
          killTopology(TOPOLOGY_NAME);
          log.info("Shut down topoloy '{}'", TOPOLOGY_NAME);
        }

      });
    }

  }

  private static void killTopology(String name) throws NotAliveException, TException {
    val client = NimbusClient.getConfiguredClient(Utils.readStormConfig()).getClient();
    val killOpts = new KillOptions();

    client.killTopologyWithOpts(name, killOpts);
  }

}
