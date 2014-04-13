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
import io.fstream.core.model.Rate;
import lombok.NoArgsConstructor;
import lombok.val;

import org.tomdz.storm.esper.EsperBolt;

import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.StringScheme;
import storm.kafka.ZkHosts;
import backtype.storm.Config;
import backtype.storm.generated.StormTopology;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.IBasicBolt;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.IRichSpout;
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
    builder.setBolt("logger", newLoggingBolt()).shuffleGrouping(spoutId);
    builder.setBolt("compute", newComputeBolt()).shuffleGrouping(spoutId);

    return builder.createTopology();
  }

  public static IRichSpout newKafkaSpout() {
    val hosts = new ZkHosts("localhost:21818");
    hosts.refreshFreqSecs = 1;
    val kafkaConf = new SpoutConfig(hosts, "test", "/test", "id");
    kafkaConf.scheme = new SchemeAsMultiScheme(new StringScheme());

    return new KafkaSpout(kafkaConf);
  }

  public static IBasicBolt newLoggingBolt() {
    return new LoggingBolt();
  }

  public static IRichBolt newComputeBolt() {
    // from("esper://events?eql=" +
    // "SELECT " +
    // "  symbol, " +
    // "  SUM(ask) AS totalAsk, " +
    // "  AVG(bid) AS avgBid, " +
    // "  COUNT(*) AS count " +
    // "FROM " +
    // "  " + Rate.class.getName() + ".win:time_batch(5 sec) " +
    // "GROUP BY " +
    // "  symbol")
    // .log("output: '${body.properties}'");
    //
    // from("esper://events?eql=" +
    // "SELECT " +
    // "  CAST(ask, float) / CAST(prior(1, ask), float) AS askPercentChange " +
    // "FROM " +
    // "  " + Rate.class.getName() + "")
    // .log("output: '${body.properties}'");

    // @formatter:off
    return new EsperBolt.Builder()
        .inputs()
          .aliasComponent("some-spout")
          .withFields("a", "b")
          .ofType(Rate.class)
          .toEventType("Rate")
        .outputs()
          .outputs().onDefaultStream().emit("min", "max")
        .statements()
          .add("select max(bid) as max, min(bid) as min from " + Rate.class.getName() + ".win:length_batch(4)")
        .build();
    // @formatter:on
  }

}
