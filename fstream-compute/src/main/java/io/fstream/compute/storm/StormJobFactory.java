/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.storm;

import static io.fstream.core.model.topic.Topic.ALERTS;
import static io.fstream.core.model.topic.Topic.METRICS;
import static io.fstream.core.model.topic.Topic.RATES;
import static java.util.UUID.randomUUID;
import io.fstream.compute.bolt.AlertBolt;
import io.fstream.compute.bolt.EsperBolt;
import io.fstream.compute.bolt.KafkaBolt;
import io.fstream.compute.bolt.LoggingBolt;
import io.fstream.compute.bolt.MetricBolt;
import io.fstream.compute.config.KafkaProperties;
import io.fstream.compute.config.StormProperties;
import io.fstream.core.model.definition.Alert;
import io.fstream.core.model.definition.Metric;
import io.fstream.core.model.state.State;
import io.fstream.core.model.topic.Topic;
import io.fstream.core.util.Codec;

import java.util.List;
import java.util.UUID;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.StringScheme;
import storm.kafka.ZkHosts;
import backtype.storm.Config;
import backtype.storm.generated.StormTopology;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.TopologyBuilder;

import com.google.common.collect.ImmutableList;

/**
 * Factory responsible for creating fully initialized {@link StormJob} instances.
 */
@Component
public class StormJobFactory {

  /**
   * Constants.
   */
  private static final int PARALLELISM = 1;
  private static final String FSTREAM_ZK_ROOT = "/fstream/storm";
  private static final String TOPIC_CONSUMER_ID_PREFIX = "storm-kafka-spout";

  /**
   * Configuration.
   */
  @Value("${zk.connect}")
  private String zkConnect;
  @Autowired
  private KafkaProperties kafkaProperties;
  @Autowired
  private StormProperties stormProperties;

  public StormJob createJob(@NonNull State state) {
    return new StormJob(calculateJobId(), createConfig(state), createTopology());
  }

  public StormJob createAlertJob(@NonNull Alert alert, List<String> symbols, List<String> common) {
    // Singleton alert
    val state = new State();
    state.setAlerts(ImmutableList.of(alert));
    state.setSymbols(symbols);
    state.setStatements(common);

    return createJob(state);
  }

  public StormJob createMetricJob(@NonNull Metric metric, List<String> symbols, List<String> common) {
    // Singleton metric
    val state = new State();
    state.setMetrics(ImmutableList.of(metric));
    state.setSymbols(symbols);
    state.setStatements(common);

    return createJob(state);
  }

  @SneakyThrows
  private Config createConfig(State state) {
    val config = new Config();
    config.setDebug(stormProperties.isDebug());

    // Serialize state
    config.put(KafkaBolt.KAFKA_BROKER_PROPERTIES_CONFIG_NAME, kafkaProperties.getProducerProperties());
    config.put(EsperBolt.STATEMENTS_CONFIG_KEY, Codec.encodeText(state.getStatements()));
    config.put(AlertBolt.ALERTS_CONFIG_KEY, Codec.encodeText(state.getAlerts()));
    config.put(MetricBolt.METRICS_CONFIG_KEY, Codec.encodeText(state.getMetrics()));

    // Parallelism
    config.setMaxTaskParallelism(PARALLELISM);
    config.setNumWorkers(PARALLELISM);

    return config;
  }

  private StormTopology createTopology() {

    /**
     * Setup
     */
    // IDs
    val ratesSpoutId = "rates-spout";
    val alertsSpoutId = "alerts-spout";
    val alertsBoltId = "alerts-bolt";
    val alertsKafkaBoltId = "alerts-kafka-bolt";
    val metricsBoltId = "metrics-bolt";
    val metricsKafkaBoltId = "metrics-kafka-bolt";
    val loggerBoltId = "logger-bolt";

    val parallelismHint = PARALLELISM;
    val builder = new TopologyBuilder();

    /**
     * Spouts
     */

    // Rates
    builder.setSpout(ratesSpoutId, createKafkaSpout(zkConnect, RATES), parallelismHint);

    // Alerts
    builder.setSpout(alertsSpoutId, createKafkaSpout(zkConnect, ALERTS), parallelismHint);

    /**
     * Bolts
     */

    // Alerts
    builder.setBolt(alertsBoltId, new AlertBolt())
        .shuffleGrouping(ratesSpoutId);
    builder.setBolt(alertsKafkaBoltId, new KafkaBolt<String, String>())
        .shuffleGrouping(alertsBoltId)
        .addConfiguration(KafkaBolt.KAFKA_TOPIC_CONFIG_NAME, ALERTS.getId());

    // Metrics
    builder.setBolt(metricsBoltId, new MetricBolt())
        .shuffleGrouping(ratesSpoutId)
        .shuffleGrouping(alertsSpoutId);
    builder.setBolt(metricsKafkaBoltId, new KafkaBolt<String, String>())
        .shuffleGrouping(metricsBoltId)
        .addConfiguration(KafkaBolt.KAFKA_TOPIC_CONFIG_NAME, METRICS.getId());

    // Logging
    builder.setBolt(loggerBoltId, new LoggingBolt())
        .shuffleGrouping(alertsBoltId)
        .shuffleGrouping(metricsBoltId);

    /**
     * Create
     */

    return builder.createTopology();
  }

  private static IRichSpout createKafkaSpout(String zkConnect, Topic topic) {
    val hosts = new ZkHosts(zkConnect);
    val zkRoot = calculateTopicZkRoot(topic);
    val consumerId = calculateTopicConsumerId(topic);

    // Collect configuration
    val kafkaConf = new SpoutConfig(hosts, topic.getId(), zkRoot, consumerId);
    kafkaConf.scheme = new SchemeAsMultiScheme(new StringScheme());

    return new KafkaSpout(kafkaConf);
  }

  private static String calculateJobId() {
    return UUID.randomUUID().toString();
  }

  private static String calculateTopicZkRoot(Topic topic) {
    // The root path in ZooKeeper for the spout to store the consumer offsets
    val zkRoot = FSTREAM_ZK_ROOT + "/kafka-" + topic.getId();

    return zkRoot;
  }

  private static String calculateTopicConsumerId(Topic topic) {
    // The unique id for this consumer for storing the consumer offsets in ZooKeeper
    val consumerId = TOPIC_CONSUMER_ID_PREFIX + "-" + topic.getId() + "-" + randomUUID();

    return consumerId;
  }

}
