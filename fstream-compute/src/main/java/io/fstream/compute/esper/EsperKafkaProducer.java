/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.esper;

import static io.fstream.core.util.Maps.getProperties;
import io.fstream.core.config.KafkaProperties;
import io.fstream.core.model.event.Event;
import io.fstream.core.model.event.EventType;
import io.fstream.core.model.topic.Topic;
import io.fstream.core.util.Codec;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("kafka")
public class EsperKafkaProducer implements EsperProducer {

  /**
   * Constants.
   */
  private static final String KAFKA_TOPIC_KEY = "1";

  /**
   * Dependencies.
   */
  @NonNull
  private final KafkaProperties kafka;
  @NonNull
  private final Producer<String, String> producer;

  @Autowired
  public EsperKafkaProducer(KafkaProperties kafka) {
    this.kafka = kafka;
    this.producer = createProducer();
  }

  @Override
  public void send(Event event) {
    val value = Codec.encodeText(event);
    val topicName = resolveTopic(event).getId();
    val message = new KeyedMessage<String, String>(topicName, KAFKA_TOPIC_KEY, value);
    producer.send(message);
  }

  private Topic resolveTopic(Event event) {
    return event.getType() == EventType.ALERT ? Topic.ALERTS : Topic.METRICS;
  }

  @SneakyThrows
  private Producer<String, String> createProducer() {
    val producerConfig = new ProducerConfig(getProperties(kafka.getProducerProperties()));

    return new Producer<>(producerConfig);
  }

}