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
import io.fstream.compute.bolt.AlertBolt;
import io.fstream.compute.bolt.EsperBolt;
import io.fstream.compute.bolt.KafkaBolt;
import io.fstream.compute.bolt.LoggingBolt;
import io.fstream.compute.bolt.MetricBolt;
import io.fstream.compute.config.ComputeProperties;
import io.fstream.compute.config.KafkaProperties;
import io.fstream.core.util.Codec;
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

/**
 * Factory for storm component creation.
 * <p>
 * @see https://github.com/nathanmarz/storm-contrib/tree/master/storm-kafka
 */
@NoArgsConstructor(access = PRIVATE)
public final class StormFactory {

  /**
   * Constants.
   */
  private static final String RATES_TOPIC_NAME = "rates";
  private static final String ALERTS_TOPIC_NAME = "alerts";

  @SneakyThrows
  public static Config newStormConfig(boolean local, KafkaProperties kafkaProperties, ComputeProperties compute) {
    val config = new Config();
    config.setDebug(true);

    // TODO: Configure per bolt?
    config.put(KafkaBolt.KAFKA_BROKER_PROPERTIES, kafkaProperties.getProducerProperties());
    config.put(KafkaBolt.TOPIC, ALERTS_TOPIC_NAME);

    config.put(EsperBolt.STATEMENTS_CONFIG_KEY, Codec.encodeText(compute.getStatements()));
    config.put(AlertBolt.ALERTS_CONFIG_KEY, Codec.encodeText(compute.getAlerts()));
    config.put(MetricBolt.METRICS_CONFIG_KEY, Codec.encodeText(compute.getMetrics()));

    if (local) {
      config.setMaxTaskParallelism(3);
    } else {
      config.setNumWorkers(3);
    }

    return config;
  }

  public static StormTopology newStormTopology(String zkConnect) {
    val spoutId = "fstream-rates";

    val builder = new TopologyBuilder();

    // Input
    builder.setSpout(spoutId, newKafkaSpout(zkConnect), 2);

    // Paths
    builder.setBolt("rateLogger", newLoggingBolt()).shuffleGrouping(spoutId);
    builder.setBolt("alerts", newAlertBolt()).shuffleGrouping(spoutId);
    builder.setBolt("metrics", newMetricBolt()).shuffleGrouping(spoutId);
    builder.setBolt("kafka-alerts", newKafkaBolt()).shuffleGrouping("alerts");
    builder.setBolt("kafka-metrics", newKafkaBolt()).shuffleGrouping("metrics");

    return builder.createTopology();
  }

  public static KafkaBolt<String, String> newKafkaBolt() {
    return new KafkaBolt<String, String>();
  }

  public static IRichSpout newKafkaSpout(String zkConnect) {
    val hosts = new ZkHosts(zkConnect);
    hosts.refreshFreqSecs = 1;

    val kafkaConf = new SpoutConfig(
        hosts,// list of Kafka brokers
        RATES_TOPIC_NAME, // Topic to read from
        "/kafkastorm", // The root path in Zookeeper for the spout to store the consumer offsets
        "discovery"); // An id for this consumer for storing the consumer offsets in Zookeeper

    kafkaConf.scheme = new SchemeAsMultiScheme(new StringScheme());

    return new KafkaSpout(kafkaConf);
  }

  public static IBasicBolt newLoggingBolt() {
    return new LoggingBolt();
  }

  public static IRichBolt newAlertBolt() {
    return new AlertBolt();
  }

  public static IRichBolt newMetricBolt() {
    return new MetricBolt();
  }

}
