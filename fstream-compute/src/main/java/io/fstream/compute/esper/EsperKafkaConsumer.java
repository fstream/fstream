/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
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
import io.fstream.core.model.topic.Topic;
import io.fstream.core.util.Codec;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.storm.guava.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.AbstractExecutionThreadService;

@Slf4j
@Component
@Profile("esper")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EsperKafkaConsumer extends AbstractExecutionThreadService {

  /**
   * Dependencies.
   */
  @NonNull
  private final KafkaProperties kafka;

  @Getter
  private final BlockingQueue<Event> queue = new ArrayBlockingQueue<>(10);

  /**
   * State.
   */
  private ConsumerConnector consumerConnector;

  @PostConstruct
  public void init() throws Exception {
    startAsync();
  }

  @PreDestroy
  public void destroy() throws Exception {
    stopAsync();

    consumerConnector.commitOffsets();
    consumerConnector.shutdown();
  }

  @Override
  protected void run() throws Exception {
    log.info("Running...");
    this.consumerConnector = createConsumerConnector();
    val streams = createStreams();
    val executor = Executors.newFixedThreadPool(streams.size());
    for (val entry : streams.entrySet()) {
      KafkaStream<byte[], byte[]> stream = entry.getValue().get(0);

      log.info("Submitting consumer for {}", entry.getKey());
      executor.submit(() -> {
        for (MessageAndMetadata<byte[], byte[]> messageAndMetadata : stream) {
          try {
            byte[] message = messageAndMetadata.message();

            if (log.isDebugEnabled()) {
              String text = new String(message);
              log.debug("Received '{}' topic message at offset {}: {}",
                  messageAndMetadata.topic(), messageAndMetadata.offset(), text);
            }

            Event event = Codec.decodeBytes(message, Event.class);
            queue.put(event);
          } catch (Exception e) {
            log.error("Error procesing message {}: {}", messageAndMetadata, e);
          }
        }
      });
    }
  }

  private ConsumerConnector createConsumerConnector() {
    return Consumer.createJavaConsumerConnector(new ConsumerConfig(getProperties(kafka.getConsumerProperties())));
  }

  private Map<String, List<KafkaStream<byte[], byte[]>>> createStreams() {
    val map = Maps.<String, Integer> newHashMap();
    map.put(Topic.TRADES.getId(), 1);
    map.put(Topic.ORDERS.getId(), 1);
    map.put(Topic.QUOTES.getId(), 1);
    map.put(Topic.ALERTS.getId(), 1);

    return consumerConnector.createMessageStreams(map);
  }

}
