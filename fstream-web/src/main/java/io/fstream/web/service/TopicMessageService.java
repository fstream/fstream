/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.web.service;

import static com.google.common.collect.ImmutableMap.of;
import static io.fstream.core.util.Maps.getProperties;
import io.fstream.core.config.KafkaProperties;
import io.fstream.core.model.topic.Topic;

import java.util.List;

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
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;

import com.google.common.util.concurrent.AbstractExecutionThreadService;

@Slf4j
@RequiredArgsConstructor
public class TopicMessageService extends AbstractExecutionThreadService {

  /**
   * Configuration.
   */
  protected final Topic topic;

  /**
   * Dependencies.
   */
  @Setter
  @Autowired
  protected SimpMessagingTemplate template;
  @Setter
  @Autowired
  protected KafkaProperties kafka;

  /**
   * State.
   */
  private KafkaStream<byte[], byte[]> stream;
  private ConsumerConnector consumerConnector;

  @PostConstruct
  public void init() throws Exception {
    log.info("Initializing '{}' service...", getTopicName());
    this.consumerConnector = createConsumerConnector();
    this.stream = createStream();

    startAsync();
    log.info("Finished initializing '{}' service", getTopicName());
  }

  @PreDestroy
  public void destroy() throws Exception {
    log.info("Destroying '{}' service...", getTopicName());
    stopAsync();

    consumerConnector.commitOffsets();
    consumerConnector.shutdown();
    log.info("Finished destroying '{}' service...", getTopicName());
  }

  @Override
  protected void run() throws Exception {
    log.info("Reading from '{}' and writing to '{}'", getTopicName(), getMessageDestination());

    for (val messageAndMetadata : stream) {
      if (!isRunning()) {
        return;
      }

      try {
        val message = messageAndMetadata.message();

        if (log.isDebugEnabled()) {
          val text = new String(message);
          log.debug("Received '{}' topic message at offset {}: {}",
              messageAndMetadata.topic(), messageAndMetadata.offset(), text);
        }

        template.send(getMessageDestination(), convert(message));
      } catch (Exception e) {
        log.error("Error procesing message {}: {}", messageAndMetadata, e);
      }
    }
  }

  private static Message<byte[]> convert(byte[] message) {
    return MessageBuilder.withPayload(message).build();
  }

  private ConsumerConnector createConsumerConnector() {
    return Consumer.createJavaConsumerConnector(new ConsumerConfig(getProperties(kafka.getConsumerProperties())));
  }

  private KafkaStream<byte[], byte[]> createStream() {
    val topicName = getTopicName();
    val topicStreamCount = 1;

    val topicMessageStreams = consumerConnector.createMessageStreams(of(topicName, topicStreamCount));

    // Lombok: No val here
    List<KafkaStream<byte[], byte[]>> streams = topicMessageStreams.get(topicName);

    return streams.get(0);
  }

  private String getTopicName() {
    return topic.getId();
  }

  private String getMessageDestination() {
    return "/topic/" + getTopicName();
  }

}
