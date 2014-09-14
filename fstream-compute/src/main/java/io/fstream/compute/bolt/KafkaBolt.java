/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.bolt;

import java.util.Map;
import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;

/**
 * Bolt implementation that can send Tuple data to Kafka
 * <p/>
 * It expects the producer configuration and topic in storm config under
 * <p/>
 * 'kafka.broker.properties' and 'topic'
 * <p/>
 * respectively.
 */
@Slf4j
public class KafkaBolt<K, V> extends BaseRichBolt {

  /**
   * Kafka config name constants.
   */
  public static final String KAFKA_TOPIC_CONFIG_NAME = "topic";
  public static final String KAFKA_BROKER_PROPERTIES_CONFIG_NAME = "kafka.broker.properties";

  /**
   * Bolt field name constants.
   */
  public static final String BOLT_KEY_FIELD_NAME = "key";
  public static final String BOLT_MESSAGE_FILED_NAME = "message";

  /**
   * State.
   */
  private Producer<K, V> producer;
  private OutputCollector collector;
  private String topic;

  @Override
  @SuppressWarnings("rawtypes")
  public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
    this.producer = new Producer<K, V>(createProducerConfig(stormConf));
    this.topic = (String) stormConf.get(KAFKA_TOPIC_CONFIG_NAME);
    this.collector = collector;

    log.info("Registered topic '{}'", topic);
  }

  @Override
  public void execute(Tuple input) {
    val key = getKey(input);
    val message = getMessage(input);

    try {
      send(key, message);
    } catch (Exception ex) {
      log.error("Could not send message with key '" + key + "' and value '" + message + "'", ex);
    } finally {
      collector.ack(input);
    }
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    // No-op
  }

  private ProducerConfig createProducerConfig(Map<?, ?> stormConf) {
    val configMap = (Map<?, ?>) stormConf.get(KAFKA_BROKER_PROPERTIES_CONFIG_NAME);
    val properties = new Properties();
    properties.putAll(configMap);

    return new ProducerConfig(properties);
  }

  private void send(K key, V message) {
    producer.send(new KeyedMessage<K, V>(topic, key, message));
  }

  @SuppressWarnings("unchecked")
  private K getKey(Tuple input) {
    return input.contains(BOLT_KEY_FIELD_NAME) ? (K) input.getValueByField(BOLT_KEY_FIELD_NAME) : null;
  }

  @SuppressWarnings("unchecked")
  private V getMessage(Tuple input) {
    return (V) input.getValueByField(BOLT_MESSAGE_FILED_NAME);
  }

}