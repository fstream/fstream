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
import io.fstream.compute.factory.StormFactory;

import javax.annotation.PostConstruct;

import lombok.Setter;
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

  private final String name = "kafka";

  @Value("${local}")
  private boolean local;
  @Autowired
  private EsperProperties properties;

  @PostConstruct
  public void execute() throws Exception {
    // Setup
    val topology = StormFactory.newStormTopology();
    val config = StormFactory.newStormConfig(local, properties.getEpl());

    if (local) {
      log.info("Submitting local topology...");
      val cluster = new LocalCluster();
      cluster.submitTopology(name, config, topology);

      cluster.shutdown();
    } else {
      log.info("Submitting cluster topology...");
      StormSubmitter.submitTopology(name, config, topology);

      killTopology(name);
    }
  }

  private static void killTopology(String name) throws NotAliveException, TException {
    val client = NimbusClient.getConfiguredClient(Utils.readStormConfig()).getClient();
    val killOpts = new KillOptions();

    client.killTopologyWithOpts(name, killOpts);
  }

}
