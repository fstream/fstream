/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.storm;

import static backtype.storm.Config.STORM_ZOOKEEPER_PORT;
import static backtype.storm.Config.STORM_ZOOKEEPER_SERVERS;
import static io.fstream.core.model.topic.Topic.ALERTS;
import static io.fstream.core.model.topic.Topic.METRICS;
import static io.fstream.core.model.topic.Topic.RATES;
import static io.fstream.core.util.ZooKeepers.parseZkPort;
import static io.fstream.core.util.ZooKeepers.parseZkServers;
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
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
@Component
public class StormJobFactory {

  /**
   * Constants.
   */
  private static final int NUM_WORKERS = 1;
  private static final int TASK_PARALLELISM = 1;
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

  public StormJob createAlertJob(@NonNull Alert alert, List<String> symbols, List<String> common) {
    // Single statement per alert
    val state = new State();
    state.setAlerts(ImmutableList.of(alert));
    state.setSymbols(symbols);
    state.setStatements(common);

    return createJob("alert" + "-" + alert.getId(), state);
  }

  public StormJob createMetricJob(@NonNull Metric metric, List<String> symbols, List<String> common) {
    // Single statement per metric
    val state = new State();
    state.setMetrics(ImmutableList.of(metric));
    state.setSymbols(symbols);
    state.setStatements(common);

    return createJob("metric" + "-" + metric.getId(), state);
  }

  private StormJob createJob(@NonNull String prefix, @NonNull State state) {
    val jobId = createJobId(prefix);

    return new StormJob(jobId, createConfig(state), createTopology(prefix, jobId, state));
  }

  @SneakyThrows
  private Config createConfig(State state) {
    log.info("Creating storm config using storm properties: {}", stormProperties);
    val config = new Config();
    config.putAll(stormProperties.getProperties());
    config.setDebug(stormProperties.isDebug());

    log.info("Configuring zookeeper to {}:{}", parseZkServers(zkConnect), parseZkPort(zkConnect));
    config.put(STORM_ZOOKEEPER_SERVERS, parseZkServers(zkConnect));
    config.put(STORM_ZOOKEEPER_PORT, parseZkPort(zkConnect));

    // Per topology configuration
    // config.put(Config.TOPOLOGY_WORKER_CHILDOPTS, "-Xmx1024m");

    // Serialize state
    log.info("Creating storm kafka config using kafka producer properties: {}", kafkaProperties.getProducerProperties());
    config.put(KafkaBolt.KAFKA_BROKER_PROPERTIES, kafkaProperties.getProducerProperties());
    config.put(EsperBolt.STATEMENTS_CONFIG_KEY, Codec.encodeText(state.getStatements()));
    config.put(AlertBolt.ALERTS_CONFIG_KEY, Codec.encodeText(state.getAlerts()));
    config.put(MetricBolt.METRICS_CONFIG_KEY, Codec.encodeText(state.getMetrics()));

    // Parallelism
    log.info("Configuring storm max task parallelism to {} and num workers to {}", TASK_PARALLELISM, NUM_WORKERS);
    config.setMaxTaskParallelism(TASK_PARALLELISM);
    config.setNumWorkers(NUM_WORKERS);

    return config;
  }

  private StormTopology createTopology(String prefix, String jobId, State state) {

    /**
     * Setup
     */
    // IDs
    val ratesSpoutId = prefix + "-rates-spout";
    val alertsSpoutId = prefix + "-alerts-spout";
    val alertsBoltId = prefix + "-alerts-bolt";
    val alertsKafkaBoltId = prefix + "-alerts-kafka-bolt";
    val metricsBoltId = prefix + "-metrics-bolt";
    val metricsKafkaBoltId = prefix + "-metrics-kafka-bolt";
    val loggerBoltId = prefix + "-logger-bolt";

    // Shorthands
    val alertsExist = !state.getAlerts().isEmpty();
    val metricsExist = !state.getMetrics().isEmpty();
    val parallelismHint = TASK_PARALLELISM;

    // State
    val topologyBuilder = new TopologyBuilder();

    /**
     * Spouts
     */

    if (alertsExist || metricsExist) {
      // Alerts and metrics Kafka rates input
      topologyBuilder.setSpout(ratesSpoutId, createKafkaSpout(prefix, zkConnect, RATES), parallelismHint);
    }

    if (metricsExist) {
      // Metrics Kafka alerts input
      topologyBuilder.setSpout(alertsSpoutId, createKafkaSpout(prefix, zkConnect, ALERTS), parallelismHint);
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

  private static IRichSpout createKafkaSpout(String prefix, String zkConnect, Topic topic) {
    val hosts = new ZkHosts(zkConnect);
    val zkRoot = getTopicZkRoot(topic);
    val consumerId = createTopicConsumerId(topic, prefix);

    // Collect configuration
    val kafkaConf = new SpoutConfig(hosts, topic.getId(), zkRoot, consumerId);
    kafkaConf.scheme = new SchemeAsMultiScheme(new StringScheme());

    return new KafkaSpout(kafkaConf);
  }

  private static String createJobId(String prefix) {
    return prefix + "-" + randomUUID().toString();
  }

  private static String getTopicZkRoot(Topic topic) {
    // The root path in ZooKeeper for the spout to store the consumer offsets
    val zkRoot = FSTREAM_ZK_ROOT + "/kafka-" + topic.getId();

    return zkRoot;
  }

  private static String createTopicConsumerId(Topic topic, String id) {
    // The unique id for this consumer for storing the consumer offsets in ZooKeeper
    val consumerId = TOPIC_CONSUMER_ID_PREFIX + "-" + topic.getId() + "-" + id;

    return consumerId;
  }
}
