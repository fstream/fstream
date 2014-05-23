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

  public static final String TOPIC = "topic";
  public static final String KAFKA_BROKER_PROPERTIES = "kafka.broker.properties";

  public static final String BOLT_KEY = "key";
  public static final String BOLT_MESSAGE = "message";

  private Object producer;
  private OutputCollector collector;
  private String topic;

  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
    val configMap = (Map) stormConf.get(KAFKA_BROKER_PROPERTIES);
    val properties = new Properties();
    properties.putAll(configMap);

    val config = new ProducerConfig(properties);
    producer = new Producer<K, V>(config);

    this.topic = (String) stormConf.get(TOPIC);
    this.collector = collector;

    log.info("Registered topic '{}'", topic);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void execute(Tuple input) {
    K key = null;
    if (input.contains(BOLT_KEY)) {
      key = (K) input.getValueByField(BOLT_KEY);
    }

    val message = (V) input.getValueByField(BOLT_MESSAGE);

    try {
      getProducer().send(new KeyedMessage<K, V>(topic, key, message));
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

  @SuppressWarnings("unchecked")
  private Producer<K, V> getProducer() {
    return (Producer<K, V>) producer;
  }

}