package io.fstream.simulate.publisher;

import io.fstream.core.model.topic.Topic;
import io.fstream.core.util.Codec;
import io.fstream.simulate.config.KafkaProperties;

import java.util.Map;
import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import lombok.NonNull;
import lombok.val;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("kafka")
public class KafkaPublisher implements Publisher {

  /**
   * Constants.
   */
  private static final Topic PUBLISH_TOPIC = Topic.TOQ;

  /**
   * Dependencies.
   */
  private final Producer<String, String> producer;

  @Autowired
  public KafkaPublisher(@NonNull KafkaProperties properties) {
    this.producer = new Producer<>(createConfig(properties.getProducerProperties()));
  }

  @Override
  public void publish(Object message) {
    val key = "1"; // TODO: this should probably be a symbol
    val value = Codec.encodeText(message);

    val keyedMessage = new KeyedMessage<String, String>(PUBLISH_TOPIC.getId(), key, value);

    producer.send(keyedMessage);
  }

  private static ProducerConfig createConfig(Map<String, String> producerProperties) {
    val config = new ProducerConfig(createProperties(producerProperties));

    return config;
  }

  private static Properties createProperties(Map<String, String> producerProperties) {
    val properties = new Properties();
    properties.putAll(producerProperties);

    return properties;
  }

}
