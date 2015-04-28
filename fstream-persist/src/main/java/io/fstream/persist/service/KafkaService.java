/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.persist.service;

import static com.google.common.collect.ImmutableMap.of;
import static io.fstream.core.util.PropertiesUtils.getProperties;
import io.fstream.core.model.event.Event;
import io.fstream.core.model.topic.Topic;
import io.fstream.core.util.Codec;
import io.fstream.persist.config.KafkaProperties;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.util.concurrent.AbstractExecutionThreadService;

@Slf4j
@RequiredArgsConstructor
public class KafkaService extends AbstractExecutionThreadService {

  /**
   * Configuration.
   */
  private final Topic topic;
  private final Class<? extends Event> eventClass;

  /**
   * Dependencies.
   */
  @Setter
  @Autowired
  private KafkaProperties kafka;
  @Setter
  @Autowired
  private PersistenceService persistenceService;

  /**
   * State.
   */
  private KafkaStream<byte[], byte[]> stream;
  private ConsumerConnector consumerConnector;

  @PostConstruct
  public void init() throws Exception {
    log.info("Initializing '{}'...", topic);
    this.consumerConnector = createConsumerConnector();
    this.stream = createStream();

    startAsync();
  }

  @PreDestroy
  public void destroy() throws Exception {
    log.info("Destroying '{}'...", topic);
    stopAsync();

    consumerConnector.commitOffsets();
    consumerConnector.shutdown();
  }

  @Override
  protected void run() throws Exception {
    log.info("Running '{}'!", topic);

    for (val messageAndMetadata : stream) {
      val message = messageAndMetadata.message();
      val text = new String(message);

      log.info("Received: {}", text);
      val rate = Codec.decodeText(text, eventClass);
      persistenceService.persist(rate);
    }
  }

  private ConsumerConnector createConsumerConnector() {
    return Consumer.createJavaConsumerConnector(new ConsumerConfig(getProperties(kafka.getConsumerProperties())));
  }

  private KafkaStream<byte[], byte[]> createStream() {
    val topicStreamCount = 1;
    val topicName = topic.getId();

    // lombok: issue with byte[] and generics
    Map<String, List<KafkaStream<byte[], byte[]>>> topicMessageStreams =
        consumerConnector.createMessageStreams(of(topicName, topicStreamCount));
    List<KafkaStream<byte[], byte[]>> streams = topicMessageStreams.get(topicName);

    return streams.get(0);
  }

}
