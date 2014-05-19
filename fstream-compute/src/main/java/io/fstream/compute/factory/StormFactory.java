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
import io.fstream.compute.bolt.ComputeBolt;
import io.fstream.compute.bolt.KafkaBolt;
import io.fstream.compute.bolt.LoggingBolt;

import java.util.List;
import java.util.Properties;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
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

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Factory for storm component creation.
 */
@NoArgsConstructor(access = PRIVATE)
public final class StormFactory {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @SneakyThrows
  public static Config newStormConfig(boolean local, List<String> epl) {
    val config = new Config();
    config.setDebug(true);

    Properties props = new Properties();
    props.put("metadata.broker.list", "localhost:6667");
    props.put("request.required.acks", "1");
    props.put("serializer.class", "kafka.serializer.StringEncoder");

    config.put(KafkaBolt.KAFKA_BROKER_PROPERTIES, props);
    config.put(KafkaBolt.TOPIC, "alerts");
    config.put(ComputeBolt.EPL, MAPPER.writeValueAsString(epl));

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
    builder.setBolt("kafka", new KafkaBolt<String, String>())
        .shuffleGrouping("compute");

    return builder.createTopology();
  }

  public static IRichSpout newKafkaSpout() {
    val hosts = new ZkHosts("localhost:2181");
    hosts.refreshFreqSecs = 1;
    val kafkaConf = new SpoutConfig(hosts, "rates", "/test", "id");
    kafkaConf.scheme = new SchemeAsMultiScheme(new StringScheme());

    return new KafkaSpout(kafkaConf);
  }

  public static IBasicBolt newLoggingBolt() {
    return new LoggingBolt();
  }

  public static IRichBolt newComputeBolt() {
    return new ComputeBolt();
  }

}
