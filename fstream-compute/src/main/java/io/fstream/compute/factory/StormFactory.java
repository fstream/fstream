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
import io.fstream.compute.bolt.AdapterBolt;
import io.fstream.compute.bolt.EsperBolt;
import io.fstream.compute.bolt.KafkaBolt;
import io.fstream.compute.bolt.LoggingBolt;
import io.fstream.core.model.Rate;

import java.util.Properties;

import lombok.NoArgsConstructor;
import lombok.val;
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

    Properties props = new Properties();
    props.put("metadata.broker.list", "localhost:6667");
    props.put("request.required.acks", "1");
    props.put("serializer.class", "kafka.serializer.StringEncoder");

    config.put(KafkaBolt.KAFKA_BROKER_PROPERTIES, props);
    config.put(KafkaBolt.TOPIC, "analysis");
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
    builder.setBolt("rateLogger", newLoggingBolt())
        .shuffleGrouping(spoutId);
    builder.setBolt("compute", newComputeBolt())
        .shuffleGrouping(spoutId);
    builder.setBolt("adapter", new AdapterBolt())
        .shuffleGrouping(spoutId);
    builder.setBolt("kafka", new KafkaBolt<String, String>())
        .shuffleGrouping("adapter");

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
    // "SELECT " +
    // "  symbol, " +
    // "  SUM(ask) AS totalAsk, " +
    // "  AVG(bid) AS avgBid, " +
    // "  COUNT(*) AS count " +
    // "FROM " +
    // "  " + Rate.class.getName() + ".win:time_batch(5 sec) " +
    // "GROUP BY " +
    // "  symbol"

    // @formatter:off
    return new EsperBolt.Builder()
        .inputs()
          .aliasComponent("compute-spout")
          .withFields("askPercentChange")
          .ofType(Rate.class)
          .toEventType("Rate")
        .outputs()
          .outputs().onDefaultStream().emit("askPercentChange")
        .statements()
          .add(
              "SELECT " +
              "  CAST(ask, float) / CAST(prior(1, ask), float) AS askPercentChange " +
              "FROM " +
              "  " + Rate.class.getName() + "")
        .build();
    // @formatter:on
  }

}
