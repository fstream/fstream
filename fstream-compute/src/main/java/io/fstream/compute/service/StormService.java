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
import io.fstream.core.model.state.State;
import io.fstream.core.model.topic.Topic;
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
  private State state;
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
    config.put(EsperBolt.STATEMENTS_CONFIG_KEY, Codec.encodeText(state.getStatements()));
    config.put(AlertBolt.ALERTS_CONFIG_KEY, Codec.encodeText(state.getAlerts()));
    config.put(MetricBolt.METRICS_CONFIG_KEY, Codec.encodeText(state.getMetrics()));

    if (stormProperties.isLocal()) {
      config.setMaxTaskParallelism(PARALLELISM);
    } else {
      config.setNumWorkers(PARALLELISM);
    }

    return config;
  }

  public StormTopology createTopology() {

    /**
     * Setup
     */
    val ratesSpoutId = "rates-spout";
    val alertsSpoutId = "alerts-spout";
    val alertsBoltId = "alerts-bolt";
    val alertsKafkaBoltId = "alerts-kafka-bolt";
    val metricsBoltId = "metrics-bolt";
    val metricsKafkaBoltId = "metrics-kafka-bolt";
    val loggerBoltId = "logger-bolt";

    val parallelismHint = 2;
    val builder = new TopologyBuilder();

    /**
     * Spouts
     */

    // Rates
    builder.setSpout(ratesSpoutId, newKafkaSpout(zkConnect, RATES), parallelismHint);

    // Alerts
    builder.setSpout(alertsSpoutId, newKafkaSpout(zkConnect, ALERTS), parallelismHint);

    /**
     * Bolts
     */

    // Alerts
    builder.setBolt(alertsBoltId, new AlertBolt())
        .shuffleGrouping(ratesSpoutId);
    builder.setBolt(alertsKafkaBoltId, new KafkaBolt<String, String>())
        .shuffleGrouping(alertsBoltId)
        .addConfiguration(KafkaBolt.TOPIC, ALERTS.getId());

    // Metrics
    builder.setBolt(metricsBoltId, new MetricBolt())
        .shuffleGrouping(ratesSpoutId)
        .shuffleGrouping(alertsSpoutId);
    builder.setBolt(metricsKafkaBoltId, new KafkaBolt<String, String>())
        .shuffleGrouping(metricsBoltId)
        .addConfiguration(KafkaBolt.TOPIC, METRICS.getId());

    // Logging
    builder.setBolt(loggerBoltId, new LoggingBolt())
        .shuffleGrouping(ratesSpoutId);

    /**
     * Create
     */

    return builder.createTopology();
  }

  private IRichSpout newKafkaSpout(String zkConnect, Topic topic) {
    // List of Kafka brokers
    val hosts = newZkHosts(zkConnect);

    // The root path in ZooKeeper for the spout to store the consumer offsets
    val zkRoot = "/fstream/storm/kafka-" + topic.getId();

    // TODO: determine if this needs to be unique
    // An id for this consumer for storing the consumer offsets in ZooKeeper
    val consumerId = "storm-kafka-spout-" + topic.getId();

    val kafkaConf = new SpoutConfig(hosts, topic.getId(), zkRoot, consumerId);
    kafkaConf.forceFromStart = true;
    kafkaConf.scheme = new SchemeAsMultiScheme(new StringScheme());

    return new KafkaSpout(kafkaConf);
  }

  private ZkHosts newZkHosts(String zkConnect) {
    val hosts = new ZkHosts(zkConnect);
    hosts.refreshFreqSecs = 1;

    return hosts;
  }

}
