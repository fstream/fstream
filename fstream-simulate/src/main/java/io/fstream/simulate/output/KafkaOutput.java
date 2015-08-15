package io.fstream.simulate.output;

import io.fstream.core.config.KafkaProperties;
import io.fstream.core.model.event.Event;
import io.fstream.core.model.event.EventType;
import io.fstream.core.model.topic.Topic;
import io.fstream.core.util.Codec;

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
public class KafkaOutput implements Output {

  /**
   * Dependencies.
   */
  private final Producer<String, String> producer;

  @Autowired
  public KafkaOutput(@NonNull KafkaProperties properties) {
    this.producer = new Producer<>(createConfig(properties.getProducerProperties()));
  }

  @Override
  public void write(Event event) {
    val key = "1"; // TODO: this should probably be a symbol
    val topic = getTopic(event);
    val value = Codec.encodeText(event);

    val keyedMessage = new KeyedMessage<String, String>(topic.getId(), key, value);

    producer.send(keyedMessage);
  }

  private Topic getTopic(Event event) {
    if (event.getType() == EventType.TRADE) {
      return Topic.TRADES;
    } else if (event.getType() == EventType.ORDER) {
      return Topic.ORDERS;
    } else if (event.getType() == EventType.QUOTE) {
      return Topic.QUOTES;
    } else if (event.getType() == EventType.SNAPSHOT) {
      return Topic.SNAPSHOTS;
    }

    throw new IllegalStateException();
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
