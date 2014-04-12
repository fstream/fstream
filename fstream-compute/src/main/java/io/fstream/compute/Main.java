/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute;

import static java.lang.System.in;
import static java.lang.System.out;
import lombok.val;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.StringScheme;
import storm.kafka.ZkHosts;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;

public class Main {

  public static void main(String[] args) throws Exception {
    val hosts = new ZkHosts("localhost:21818");
    hosts.refreshFreqSecs = 1;
    val kafkaConf = new SpoutConfig(hosts, "test", "/test", "id");
    kafkaConf.id = "0";
    kafkaConf.scheme = new SchemeAsMultiScheme(new StringScheme());

    KafkaSpout kafkaSpout = new KafkaSpout(kafkaConf);

    val builder = new TopologyBuilder();
    builder.setSpout("spout", kafkaSpout, 2);
    builder.setBolt("printer", new PrinterBolt()).shuffleGrouping("spout");

    Config config = new Config();
    config.setDebug(true);

    if (args != null && args.length > 0) {
      config.setNumWorkers(3);

      StormSubmitter.submitTopology(args[0], config, builder.createTopology());
    } else {
      config.setMaxTaskParallelism(3);

      LocalCluster cluster = new LocalCluster();
      cluster.submitTopology("kafka", config, builder.createTopology());

      out.println("\n\n*** Running compute. Press any key to shutdown\n\n");
      in.read();

      cluster.shutdown();
    }
  }

}
