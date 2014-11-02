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
import storm.kafka.bolt.KafkaBolt;
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
  private static final String FSTREAM_ZK_ROOT = "/storm";
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
    val jobId = createJobId();

    return new StormJob(jobId, createConfig(state), createTopology(jobId, state));
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
    config.put(Config.TOPOLOGY_WORKER_CHILDOPTS,
        "-Xmx1024m -Djava.system.class.loader=org.springframework.boot.loader.LaunchedURLClassLoader");
    config.put(KafkaBolt.KAFKA_BROKER_PROPERTIES, kafkaProperties.getProducerProperties());
    config.put(EsperBolt.STATEMENTS_CONFIG_KEY, Codec.encodeText(state.getStatements()));
    config.put(AlertBolt.ALERTS_CONFIG_KEY, Codec.encodeText(state.getAlerts()));
    config.put(MetricBolt.METRICS_CONFIG_KEY, Codec.encodeText(state.getMetrics()));

    // Parallelism
    config.setMaxTaskParallelism(PARALLELISM);
    config.setNumWorkers(PARALLELISM);

    return config;
  }

  private StormTopology createTopology(String jobId, State state) {

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

    // Shorthands
    val alertsExist = !state.getAlerts().isEmpty();
    val metricsExist = !state.getMetrics().isEmpty();
    val parallelismHint = PARALLELISM;

    // State
    val topologyBuilder = new TopologyBuilder();

    /**
     * Spouts
     */

    if (alertsExist || metricsExist) {
      // Alerts and metrics Kafka rates input
      topologyBuilder.setSpout(ratesSpoutId, createKafkaSpout(zkConnect, RATES), parallelismHint);
    }

    if (metricsExist) {
      // Metrics Kafka alerts input
      topologyBuilder.setSpout(alertsSpoutId, createKafkaSpout(zkConnect, ALERTS), parallelismHint);
    }

    /**
     * Bolts
     */

    if (alertsExist) {
      // Alerts Esper computation
      topologyBuilder.setBolt(alertsBoltId, new AlertBolt())
          .shuffleGrouping(ratesSpoutId);

      // Alerts Kafka output
      topologyBuilder.setBolt(alertsKafkaBoltId, new KafkaBolt<String, String>())
          .shuffleGrouping(alertsBoltId)
          .addConfiguration(KafkaBolt.TOPIC, ALERTS.getId());
    }

    if (metricsExist) {
      // Metric Esper computation
      topologyBuilder.setBolt(metricsBoltId, new MetricBolt())
          .shuffleGrouping(ratesSpoutId)
          .shuffleGrouping(alertsSpoutId);

      // Metrics Kafka output
      topologyBuilder.setBolt(metricsKafkaBoltId, new KafkaBolt<String, String>())
          .shuffleGrouping(metricsBoltId)
          .addConfiguration(KafkaBolt.TOPIC, METRICS.getId());
    }

    // Logging
    val loggingBolt = topologyBuilder.setBolt(loggerBoltId, new LoggingBolt());
    if (alertsExist) {
      loggingBolt.shuffleGrouping(alertsBoltId);
    }
    if (metricsExist) {
      loggingBolt.shuffleGrouping(metricsBoltId);
    }

    /**
     * Create
     */

    return topologyBuilder.createTopology();
  }

  private static IRichSpout createKafkaSpout(String zkConnect, Topic topic) {
    val hosts = new ZkHosts(zkConnect);
    val zkRoot = getTopicZkRoot(topic);
    val consumerId = createTopicConsumerId(topic);

    // Collect configuration
    val kafkaConf = new SpoutConfig(hosts, topic.getId(), zkRoot, consumerId);
    kafkaConf.scheme = new SchemeAsMultiScheme(new StringScheme());

    return new KafkaSpout(kafkaConf);
  }

  private static String createJobId() {
    return randomUUID().toString();
  }

  private static String getTopicZkRoot(Topic topic) {
    // The root path in ZooKeeper for the spout to store the consumer offsets
    val zkRoot = FSTREAM_ZK_ROOT + "/kafka-" + topic.getId();

    return zkRoot;
  }

  private static String createTopicConsumerId(Topic topic) {
    // The unique id for this consumer for storing the consumer offsets in ZooKeeper
    val consumerId = TOPIC_CONSUMER_ID_PREFIX + "-" + topic.getId() + "-" + randomUUID().toString();

    return consumerId;
  }
}
