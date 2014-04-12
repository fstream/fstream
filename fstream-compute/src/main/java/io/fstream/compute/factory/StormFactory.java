/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.factory;

import static lombok.AccessLevel.PRIVATE;
import io.fstream.compute.bolt.LoggingBolt;
import lombok.NoArgsConstructor;
import lombok.val;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.StringScheme;
import storm.kafka.ZkHosts;
import backtype.storm.Config;
import backtype.storm.generated.StormTopology;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;

/**
 * Factory for storm component creation.
 */
@NoArgsConstructor(access = PRIVATE)
public final class StormFactory {

  public static Config newConfig(boolean local) {
    val config = new Config();
    config.setDebug(true);

    if (local) {
      config.setMaxTaskParallelism(3);
    } else {
      config.setNumWorkers(3);
    }

    return config;
  }

  public static StormTopology newStormTopology() {
    val spoutId = "fstream-rates";
    val builder = new TopologyBuilder();
    builder.setSpout(spoutId, newKafkaSpout(), 2);
    builder.setBolt("logger", new LoggingBolt()).shuffleGrouping(spoutId);

    return builder.createTopology();
  }

  public static KafkaSpout newKafkaSpout() {
    val hosts = new ZkHosts("localhost:21818");
    hosts.refreshFreqSecs = 1;
    val kafkaConf = new SpoutConfig(hosts, "test", "/test", "id");
    kafkaConf.scheme = new SchemeAsMultiScheme(new StringScheme());

    return new KafkaSpout(kafkaConf);
  }

}
