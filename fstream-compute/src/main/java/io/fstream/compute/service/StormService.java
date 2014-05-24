/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.service;

import static io.fstream.core.model.topic.Topic.ALERTS;
import static io.fstream.core.model.topic.Topic.METRICS;
import static io.fstream.core.model.topic.Topic.RATES;
import io.fstream.compute.bolt.AlertBolt;
import io.fstream.compute.bolt.EsperBolt;
import io.fstream.compute.bolt.KafkaBolt;
import io.fstream.compute.bolt.LoggingBolt;
import io.fstream.compute.bolt.MetricBolt;
import io.fstream.compute.config.ComputeProperties;
import io.fstream.compute.config.KafkaProperties;
import io.fstream.compute.config.StormProperties;
import io.fstream.core.util.Codec;
import lombok.SneakyThrows;
import lombok.val;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.StringScheme;
import storm.kafka.ZkHosts;
import backtype.storm.Config;
import backtype.storm.generated.StormTopology;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.TopologyBuilder;

/**
 * Factory for storm component creation.
 * <p>
 * @see https://github.com/nathanmarz/storm-contrib/tree/master/storm-kafka
 */
@Service
public class StormService {

  /**
   * Constants.
   */
  private static final int PARALLELISM = 3;

  /**
   * Configuration.
   */
  @Value("${zk.connect}")
  private String zkConnect;
  @Autowired
  private StormProperties stormProperties;
  @Autowired
  private KafkaProperties kafkaProperties;
  @Autowired
  private ComputeProperties computeProperties;

  @SneakyThrows
  public Config createConfig() {
    val config = new Config();
    config.setDebug(true);

    config.put(KafkaBolt.KAFKA_BROKER_PROPERTIES, kafkaProperties.getProducerProperties());
    config.put(EsperBolt.STATEMENTS_CONFIG_KEY, Codec.encodeText(computeProperties.getStatements()));
    config.put(AlertBolt.ALERTS_CONFIG_KEY, Codec.encodeText(computeProperties.getAlerts()));
    config.put(MetricBolt.METRICS_CONFIG_KEY, Codec.encodeText(computeProperties.getMetrics()));

    if (stormProperties.isLocal()) {
      config.setMaxTaskParallelism(PARALLELISM);
    } else {
      config.setNumWorkers(PARALLELISM);
    }

    return config;
  }

  public StormTopology createTopology() {
    val builder = new TopologyBuilder();

    // Rates
    val ratesId = "fstream-rates";
    val parallelismHint = 2;
    builder.setSpout(ratesId, newKafkaSpout(zkConnect), parallelismHint);

    // Alerts
    val alertsId = "fstream-alerts";
    builder.setBolt(alertsId, new AlertBolt()).shuffleGrouping(ratesId);
    builder.setBolt("kafka-alerts", new KafkaBolt<String, String>()).shuffleGrouping(alertsId)
        .addConfiguration(KafkaBolt.TOPIC, METRICS.getId());

    // Metrics
    val metricsId = "fstream-metrics";
    builder.setBolt(metricsId, new MetricBolt()).shuffleGrouping(ratesId);
    builder.setBolt("kafka-metrics", new KafkaBolt<String, String>()).shuffleGrouping(metricsId)
        .addConfiguration(KafkaBolt.TOPIC, ALERTS.getId());

    // Logging
    val loggerId = "fstream-logger";
    builder.setBolt(loggerId, new LoggingBolt()).shuffleGrouping(ratesId);

    return builder.createTopology();
  }

  private IRichSpout newKafkaSpout(String zkConnect) {
    // List of Kafka brokers
    val hosts = newZkHosts(zkConnect);

    // Topic to read from
    val topic = RATES.getId();

    // The root path in ZooKeeper for the spout to store the consumer offsets
    val zkRoot = "/fstream/storm/kafka";

    // An id for this consumer for storing the consumer offsets in ZooKeeper
    val consumerId = "fstream-storm-kafka-spout";

    val kafkaConf = new SpoutConfig(hosts, topic, zkRoot, consumerId);
    kafkaConf.scheme = new SchemeAsMultiScheme(new StringScheme());

    return new KafkaSpout(kafkaConf);
  }

  private ZkHosts newZkHosts(String zkConnect) {
    val hosts = new ZkHosts(zkConnect);
    hosts.refreshFreqSecs = 1;

    return hosts;
  }

}
