/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.persistence.service;

import static io.fstream.core.util.PropertiesUtils.getProperties; 
import static com.google.common.collect.ImmutableMap.of;
import io.fstream.core.model.event.TickEvent;
import io.fstream.persistence.config.KafkaProperties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import lombok.Setter;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.AbstractExecutionThreadService;

@Slf4j
@Service
public class KafkaService extends AbstractExecutionThreadService {
  
  /**
   * Constants.
   */
  private static final ObjectMapper MAPPER = new ObjectMapper();

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
   * Configuration.
   */
  private final String topicName = "rates";

  /**
   * State.
   */
  private KafkaStream<byte[], byte[]> stream;
  private ConsumerConnector consumerConnector;

  @PostConstruct
  public void init() throws Exception {
    log.info("Initializing...");
    this.consumerConnector = createConsumerConnector();
    this.stream = createStream();

    startAsync();
  }

  @PreDestroy
  public void destroy() throws Exception {
    log.info("Destroying...");
    stopAsync();

    consumerConnector.commitOffsets();
    consumerConnector.shutdown();
  }

  @Override
  protected void run() throws Exception {
    log.info("Running!");

    for (val messageAndMetadata : stream) {
      val message = messageAndMetadata.message();
      val text = new String(message);

      log.info("Received: {}", text);
      val rate = MAPPER.readValue(text, TickEvent.class);
      persistenceService.persist(rate);
    }
  }

  private ConsumerConnector createConsumerConnector() {
    return Consumer.createJavaConsumerConnector(new ConsumerConfig(getProperties(kafka.getConsumerProperties())));
  }

  private KafkaStream<byte[], byte[]> createStream() {
    val topicStreamCount = 1;

    val topicMessageStreams = consumerConnector.createMessageStreams(of(topicName, topicStreamCount));
    val streams = topicMessageStreams.get(topicName);

    return streams.get(0);
  }

}
